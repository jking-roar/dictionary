package dict;

import lang.Definition;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WiktionaryDefinitionsReader implements Reader {
    private Map<String, List<Definition>> definitions = new HashMap<String, List<Definition>>() {
        @Override
        public List<Definition> get(Object key) {
            List<Definition> value = super.get(key);
            if(value == null) {
                value = new ArrayList<Definition>();
                put((String) key, value);
            }
            return value;
        }
    };


    public WiktionaryDefinitionsReader(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                Definition d = processOneLine(line);
                definitions.get(d.word).add(d);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("where's the data?  Try download.sh before using", e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Definition processOneLine(String definitionLine) {
        String[] lineParts = definitionLine.split("\t");
        String meaning = lineParts[3];
        if (meaning.startsWith("# ")) {
            meaning = meaning.substring(2);
        }

        meaning = meaning.replaceAll("\\[\\[([^\\]]+)\\]\\]", "$1");
        meaning = meaning.replaceAll("\\[\\[([^\\]]+)|[^\\]]+\\]\\]", "$1");

        return new Definition(lineParts[1], lineParts[2], meaning);
    }

    @Override
    public List<Definition> getDefinitions(String word) {
        return definitions.get(word);
    }
}
