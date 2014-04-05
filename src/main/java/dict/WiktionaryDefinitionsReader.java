package dict;

import lang.Definition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WiktionaryDefinitionsReader implements Reader {
    private Map<String, List<Definition>> definitions = new HashMap<String, List<Definition>>() {
        @Override
        public List<Definition> get(Object key) {
            List<Definition> value = super.get(key);
            if (value == null) {
                value = new ArrayList<Definition>();
                put((String) key, value);
            }
            return value;
        }
    };
    private Set<Definition> allDefinitions = new LinkedHashSet<Definition>();
    private static final String SBS = "\\{";
    private static final String BRACE_START = SBS + SBS;
    private static final String SBE = "\\}";
    private static final String BRACE_END = SBE + SBE;
    private static final String SPLITTER = "\\|";
    private static final String PART_OF_BRACE_EXPRESSION = many(not(or(SBE, SPLITTER)));
    private static final Pattern SOPLINK_PATTERN = Pattern
            .compile(capture(".*") + BRACE_START + "soplink" + capture(any(capture(SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION))))) + BRACE_END + capture(".*"));
    private static final String NON_BRACE_ENDS = many(not("\\}"));

    public WiktionaryDefinitionsReader(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            for (String line = br.readLine(); line != null; line = br.readLine()) {

                Definition d = null;
                try {
                    d = processOneLine(line);
                } catch (NotDefinition notDefinition) {
                    continue;
                }

                definitions.get(d.word).add(d);
                allDefinitions.add(d);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("where's the data?  Try download.sh before using", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Definition processOneLine(String definitionLine) throws NotDefinition {
        String[] lineParts = definitionLine.split("\t");
        String meaning = lineParts[3];
        if (meaning.startsWith("# ")) {
            meaning = meaning.substring(2);
        }

        meaning = removeRefTags(meaning);

        meaning = handleSquareBrackets(meaning);

        meaning = handleCurlyBraces(meaning);

        meaning = meaning.replaceAll("<[^>]+>", "");

        String word = lineParts[1];
        String pos = lineParts[2];
        if (pos.startsWith("Etymol")) {
            throw new NotDefinition("an etymology");
        }
        return new Definition(word, pos, meaning.replaceAll("\\|", " ").trim());
    }

    private String removeRefTags(String meaning) {
        meaning = meaning.replaceAll("<ref[^>]*>(?:(?!</ref>).).*</ref> ?", "");
        return meaning;
    }

    private String handleSquareBrackets(String meaning) {
        meaning = meaning.replaceAll("\\[\\[([^\\|\\]]*)\\|[^\\]]*\\]\\]", "$1");
        meaning = meaning.replaceAll("\\[\\[([^\\]]+)\\]\\]", "$1");
        meaning = meaning.replaceAll("\\[\\[signal\\]([^\\]])", "signal$1");
        meaning = meaning.replaceAll(".\\[http://" +
                                             "[^\\]]+" +
                                             "\\].", "");
        return meaning;
    }

    private String handleCurlyBraces(String meaning) throws NotDefinition {

        if (meaning.matches(".*" + BRACE_START + "rfdef" + ".*")) {
            throw new NotDefinition("a request for a definition");
        }
        if (meaning.matches(".*" + BRACE_START + "&lit" + ".*")) {
            throw new NotDefinition("literally translating an idiom");
        }

        //punctuation things
        meaning = meaning.replaceAll(BRACE_START + capture(PART_OF_BRACE_EXPRESSION) + BRACE_END, "$1");

        meaning = meaning.replaceAll(BRACE_START + "w" + SPLITTER + capture(PART_OF_BRACE_EXPRESSION) + BRACE_END, "$1");
        meaning = meaning.replaceAll(BRACE_START + "w" + SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER + capture(PART_OF_BRACE_EXPRESSION) + BRACE_END, "$2");

        meaning = meaning.replaceAll(BRACE_START + "Latn-def" + SPLITTER + PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) + SPLITTER +
                                             capture(many(capture(PART_OF_BRACE_EXPRESSION + optional(SPLITTER)))) + BRACE_END, "The $1 of the Latin-script letter $2.");

        // soplink
        Matcher matcher = SOPLINK_PATTERN.matcher(meaning);
        if (matcher.find()) {
            String before = matcher.group(1);
            String after = matcher.group(matcher.groupCount());
            String contents = matcher.group(2);
            meaning = before + contents.replaceAll(SPLITTER, " ").trim() + after;
        }

        // context of some domain
        meaning = meaning.replaceAll(BRACE_START + capture("context|cx") + NON_BRACE_ENDS + SBE + any(capture(" and " + SBS + capture("context|cx") + NON_BRACE_ENDS + SBE)) + SBE, "");
        meaning = meaning.replaceAll(BRACE_START + "label" + NON_BRACE_ENDS + BRACE_END, "");

        meaning = meaning.replaceAll(BRACE_START + "\\&lit" + SPLITTER + capture(NON_BRACE_ENDS) + BRACE_END, "see $1");

        meaning = meaning.replaceAll(BRACE_START + "cite" + NON_BRACE_ENDS + BRACE_END, "");

        // some form of something else
        meaning = meaning
                .replaceAll(BRACE_START + "form of" + SPLITTER + "(" + PART_OF_BRACE_EXPRESSION + ")" + SPLITTER + "(" + PART_OF_BRACE_EXPRESSION + ")" + SPLITTER + PART_OF_BRACE_EXPRESSION + BRACE_END, "$1 of $2");
        meaning = meaning.replaceAll(BRACE_START +
                                             capture(PART_OF_BRACE_EXPRESSION + "of") +
                                             SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             any(capture(SPLITTER + capture(or("", PART_OF_BRACE_EXPRESSION)))) +
                                             BRACE_END, "$1 $2"
        );

        meaning = meaning.replaceAll(BRACE_START + "taxlink" + SPLITTER + capture(PART_OF_BRACE_EXPRESSION) + any(capture(SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION)))) + BRACE_END, "$1");

        //term
        meaning = meaning.replaceAll(BRACE_START + "term" + SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER +
                                             optional(capture(SPLITTER + "lang=" + PART_OF_BRACE_EXPRESSION)) + BRACE_END, "$1");

        meaning = meaning.replaceAll(BRACE_START + "term" + SPLITTER +
                                             SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) +
                                             optional(capture(SPLITTER + "lang=" + PART_OF_BRACE_EXPRESSION)) + BRACE_END, "$1");

        meaning = meaning.replaceAll(BRACE_START + "term" + SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) + many(SPLITTER) +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) +
                                             optional(capture(SPLITTER + "lang=" + PART_OF_BRACE_EXPRESSION)) + BRACE_END, "$1 ($2)");

        meaning = meaning.replaceAll(BRACE_START + "term" + SPLITTER +
                                             PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER +
                                             "tr=" + capture(PART_OF_BRACE_EXPRESSION) +
                                             any(capture(SPLITTER + PART_OF_BRACE_EXPRESSION)) + BRACE_END, "$3");

        //{{term|chile ancho|chiles anchos|wide chilis|lang=es}}
        meaning = meaning.replaceAll(BRACE_START +
                                             "term" +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             SPLITTER +
                                             "lang=" + PART_OF_BRACE_EXPRESSION +
                                             BRACE_END, "$1 ($2)");

        meaning = meaning.replaceAll(BRACE_START +
                                             "term" +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) +
                                             SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             BRACE_END, "$1 ($3)");

        meaning = meaning.replaceAll(BRACE_START + "SI-unit" + NON_BRACE_ENDS + BRACE_END, "An SI unit");

        meaning = meaning.replaceAll(BRACE_START + capture("dated form of") + SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER + capture(PART_OF_BRACE_EXPRESSION) + BRACE_END,
                                     "$1 $3");
        // last of five, four, three, and then two things
        meaning = meaning.replaceAll(BRACE_START +
                                             PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             BRACE_END, "$1");
        meaning = meaning.replaceAll(BRACE_START +
                                             PART_OF_BRACE_EXPRESSION + SPLITTER + PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION +
                                             SPLITTER +
                                             PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) +
                                             BRACE_END, "$1");

        meaning = meaning.replaceAll(BRACE_START + PART_OF_BRACE_EXPRESSION +
                                             SPLITTER + PART_OF_BRACE_EXPRESSION +
                                             SPLITTER + capture(PART_OF_BRACE_EXPRESSION) +
                                             BRACE_END, "$1");

        meaning = meaning.replaceAll(BRACE_START + PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             "(" + PART_OF_BRACE_EXPRESSION + ")" +
                                             BRACE_END, "$1");

        //latin def
        meaning = meaning.replaceAll(" " +
                                             BRACE_START +
                                             "Latn-def" +
                                             SPLITTER +
                                             NON_BRACE_ENDS +
                                             BRACE_END, "");

        meaning = meaning.replaceAll(BRACE_START + "given name" + SPLITTER + capture(PART_OF_BRACE_EXPRESSION) + any(capture(SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION)))) + BRACE_END,
                                     "A given $1 name");

        meaning = meaning.replaceAll(BRACE_START + "term" + SPLITTER +
                                             PART_OF_BRACE_EXPRESSION + SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) + SPLITTER +
                                             optional(capture(PART_OF_BRACE_EXPRESSION)) + SPLITTER +
                                             capture(PART_OF_BRACE_EXPRESSION) + SPLITTER +
                                             "pos=" + capture(PART_OF_BRACE_EXPRESSION) +
                                             any(capture(SPLITTER + PART_OF_BRACE_EXPRESSION)) + BRACE_END, "$1 (\"$3\", $4)");

        meaning = meaning.replaceAll(BRACE_START + "surname" + any(capture(SPLITTER + optional(capture(PART_OF_BRACE_EXPRESSION)))) + BRACE_END, "A surname");

        //errors of omission
        if (meaning.matches(".*" + BRACE_START + NON_BRACE_ENDS + "$")) {
//            System.err.println("BAD! : " + meaning);
        }
        meaning = meaning.replaceAll(BRACE_START + NON_BRACE_ENDS + "$",
                                     "");
        return meaning;
    }

    private static String optional(String thing) {
        return thing + "?";
    }

    private static String any(String expr) {
        return expr + "*";
    }

    private static String many(String expr) {return expr + "+";}

    private static String capture(String expr) {return "(" + expr + ")";}

    private static String or(String a, String b) { return b + "|" + a; }

    private static String not(String thing) { return "[^" + thing + "]"; }

    @Override
    public List<Definition> getDefinitions(String word) {
        return definitions.get(word);
    }

    @Override
    public Set<Definition> allDefinitions() {
        return allDefinitions;
    }
}
