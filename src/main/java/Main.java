import dict.Reader;
import dict.WiktionaryDefinitionsReader;
import lang.Definition;

public class Main {

    public static void main(String[] args) {
        Reader reader = new WiktionaryDefinitionsReader("data/enwikt-defs-latest-en.tsv");
//        List<Definition> aDefs = reader.getDefinitions("a");
//        for(Definition d : aDefs) {
//            System.out.println(d);
//        }

        for (Definition definition : reader.allDefinitions()) {
            String gloss = definition.meaning;
            if(gloss.contains("[[") || gloss.contains("{{")) {
                System.out.println(definition.toString());
            }
        }
    }
}
