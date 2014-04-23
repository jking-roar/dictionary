import dict.Reader;
import dict.WiktionaryDefinitionsReader;
import lang.Definition;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        Reader reader = new WiktionaryDefinitionsReader("data/enwikt-defs-latest-en.tsv");
//        List<Definition> aDefs = reader.getDefinitions("a");
//        for(Definition d : aDefs) {
//            System.out.println(d);
//        }

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
}
