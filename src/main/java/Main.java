import dict.Reader;
import dict.WiktionaryDefinitionsReader;
import lang.Definition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Main {

    private static final int DEFAULT_DEPTH = 3;
    private static Reader reader;

    public static void main(String[] args) throws IOException {
        Mode mode = Mode.understand;
        if (args.length > 0) {
            try {
                mode = Mode.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                System.out.println("first argument can be one of " + Arrays.toString(Mode.values()));
                System.exit(0);
            }
        }

        reader = new WiktionaryDefinitionsReader("data/enwikt-defs-latest-en.tsv");
        if (Mode.lookup == mode) {
            lookupWords();
        } else if (Mode.understand == mode) {
            int depth = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_DEPTH;
            understand(depth);
        }
    }

    private static void understand(int depth) throws IOException {
        System.out.println("All the words to understand a word!");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Search for a word!");
            String line = br.readLine();
            if (line == null) {
                break;
            }

            line = line.trim();
            if (line.isEmpty()) {
                System.out.println("  end input to terminate!");
                continue;
            }

            Set<String> words = new LinkedHashSet<String>();
            dfs(line, words, depth);

            System.out.println(words);
            System.out.println(words.size());
        }
    }

    private static void dfs(String word, Set<String> seenWords, int depth) {
        if (seenWords.contains(word) || word.isEmpty() || depth < 1) {
            return;
        }
        seenWords.add(word);
        List<Definition> defns = reader.getDefinitions(word);
        for (Definition d : defns) {
            for (String s : d.meaning.replaceAll("\\p{Punct}", " ").split("\\s")) {
                dfs(s, seenWords, depth-1);
            }
        }
    }

    private static void lookupWords() throws IOException {
        System.out.println("Scrubbed word definitions!");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("Search for a word!");
            String line = br.readLine();
            if (line == null) {
                break;
            }

            line = line.trim();
            if (line.isEmpty()) {
                System.out.println("  end input to terminate!");
                continue;
            }

            List<Definition> defns = reader.getDefinitions(line);
            if (defns.isEmpty()) {
                System.out.println("No definitions found");
            } else {
                for (Definition d : defns) {
                    System.out.println(d);
                }
            }
        }
    }

    enum Mode {
        lookup,
        understand
    }
}
