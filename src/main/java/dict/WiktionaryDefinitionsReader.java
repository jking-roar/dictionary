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

        meaning = handleSquareBrackets(meaning);

        meaning = handleCurlyBraces(meaning);

        meaning = meaning.replaceAll("<[^>]+>", "");

        String word = lineParts[1];
        String pos = lineParts[2];
        if(pos.startsWith("Etymol")) {
            throw new NotDefinition("an etymology");
        }
        return new Definition(word, pos, meaning.replaceAll("\\|", " ").trim());
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


        String sbs = "\\{";
        String braceStart = sbs + sbs;
        String sbe = "\\}";
        String braceEnd = sbe + sbe;
        String splitter = "\\|";
        String space = " ";
        String partOfBraceExpression = many(not(or(sbe, splitter)));
        String nonBraceEnds = many(not("\\}"));

        if(meaning.matches(".*" + braceStart+"rfdef" + ".*")) {
            throw new NotDefinition("a request for a definition");
        }
        if(meaning.matches(".*" + braceStart+"&lit" + ".*")) {
            throw new NotDefinition("literally translating an idiom");
        }

        //punctuation things
        meaning = meaning.replaceAll(braceStart + capture(partOfBraceExpression) + braceEnd, "$1");

        meaning = meaning.replaceAll(braceStart + "Latn-def" + splitter + partOfBraceExpression + splitter +
                                             capture(partOfBraceExpression) + splitter +
                                             capture(many(capture(partOfBraceExpression + optional(splitter)))) + braceEnd, "The $1 of the Latin-script letter $2.");

        // context of some domain
        meaning = meaning.replaceAll(braceStart + capture("context|cx") + nonBraceEnds + sbe + any(capture(" and " + sbs + capture("context|cx") + nonBraceEnds + sbe)) + sbe, "");
        meaning = meaning.replaceAll(braceStart + "label" + nonBraceEnds + braceEnd, "");

        meaning = meaning.replaceAll(braceStart + "\\&lit" + splitter + capture(nonBraceEnds) + braceEnd, "see $1");

        meaning = meaning.replaceAll(braceStart + "cite" + nonBraceEnds + braceEnd, "");

        // some form of something else
        meaning = meaning.replaceAll(braceStart + "form of" + splitter + "(" + partOfBraceExpression + ")" + splitter + "(" + partOfBraceExpression + ")" + splitter + partOfBraceExpression + braceEnd, "$1 of $2");
        meaning = meaning.replaceAll(braceStart +
                                             capture(partOfBraceExpression + "of") +
                                             splitter +
                                             capture(partOfBraceExpression) +
                                             any(capture(splitter + capture(or("", partOfBraceExpression)))) +
                                             braceEnd, "$1 $2"
        );

        //term
        meaning = meaning.replaceAll(braceStart + "term" + splitter +
                                             optional(capture(partOfBraceExpression)) + splitter +
                                             optional(capture(splitter + "lang=" + partOfBraceExpression)) + braceEnd, "$1");

        meaning = meaning.replaceAll(braceStart + "term" + splitter +
                                             splitter +
                                             optional(capture(partOfBraceExpression)) +
                                             optional(capture(splitter + "lang=" + partOfBraceExpression)) + braceEnd, "$1");

        meaning = meaning.replaceAll(braceStart + "term" + splitter +
                                             optional(capture(partOfBraceExpression)) + many(splitter) +
                                             optional(capture(partOfBraceExpression)) +
                                             optional(capture(splitter + "lang=" + partOfBraceExpression)) + braceEnd, "$1 ($2)");;

        // last of four, three, and then two things
        meaning = meaning.replaceAll(braceStart +
                                             partOfBraceExpression +
                                             splitter +
                                             partOfBraceExpression +
                                             splitter +
                                             partOfBraceExpression + splitter +
                                             capture(partOfBraceExpression) +
                                             braceEnd, "$1");

        meaning = meaning.replaceAll(braceStart + partOfBraceExpression +
                                             splitter + partOfBraceExpression +
                                             splitter + capture(partOfBraceExpression) +
                                             braceEnd, "$1");

        meaning = meaning.replaceAll(braceStart + partOfBraceExpression + splitter +
                                             "(" + partOfBraceExpression + ")" +
                                             braceEnd, "$1");

        //latin def
        meaning = meaning.replaceAll(" " +
                                             braceStart +
                                             "Latn-def" +
                                             splitter +
                                             nonBraceEnds +
                                             braceEnd, "");

        meaning = meaning.replaceAll(braceStart + "cite web$", "");
        meaning = meaning.replaceAll(braceStart + "reference-journal$", "");

        return meaning;
    }

    private String optional(String thing) {
        return thing + "?";
    }

    private String any(String expr) {
        return expr + "*";
    }

    private String many(String expr) {return expr + "+";}

    private String capture(String expr) {return "(" + expr + ")";}

    private String or(String a, String b) { return b + "|" + a; }

    private String not(String thing) { return "[^" + thing + "]"; }

    @Override
    public List<Definition> getDefinitions(String word) {
        return definitions.get(word);
    }

    @Override
    public Set<Definition> allDefinitions() {
        return allDefinitions;
    }
}
