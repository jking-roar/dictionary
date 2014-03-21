import dict.Reader;
import dict.WiktionaryDefinitionsReader;
import lang.Definition;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        Reader reader = new WiktionaryDefinitionsReader("data/enwikt-defs-latest-en.tsv");
        List<Definition> aDefs = reader.getDefinitions("a");
        for(Definition d : aDefs) {
            System.out.println(d);
        }
    }
}
