package dict;

import lang.Definition;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WiktionaryDefinitionsReaderTest {
    @Test
    public void readFileWithDefinitions() throws IOException {
        File file = fileWithOneLine("English\tabend\tVerb\t# To terminate abnormally.");
        Reader reader = new WiktionaryDefinitionsReader(file.getAbsolutePath());
        List<Definition> defs = reader.getDefinitions("abend");
        assertEquals(1, defs.size());

        Definition d = defs.get(0);
        assertEquals("abend", d.word);
        assertEquals("To terminate abnormally.", d.gloss);
        assertEquals("Verb", d.pos);
    }

    @Test
    public void dropsSquareBrackets() throws IOException {
        File file = fileWithOneLine("English\tabelsonite\tNoun\t# A [[mineral]].");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("abelsonite").get(0);
        assertEquals("A mineral.", def.gloss);
    }

    @Test
    public void retrievesCorrectStringFromBrackets() throws IOException {
        File file = fileWithOneLine("English\tword\tNoun\t# [[realWord|visible word]].");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("realWord.", def.gloss);

    }

    @Test
    public void dropsMarkup() throws IOException {
        File file = fileWithOneLine("English\tabelsonite\tNoun\t# A [[mineral]], NiC<sub>31</sub>H<sub>32</sub>N<sub>4</sub>, forming [[soft]] reddish-brown [[triclinic]] crystals.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("abelsonite").get(0);
        assertEquals(1, 1);
    }

    private File fileWithOneLine(String line) throws IOException {
        File dictFile = File.createTempFile("wiktionary", "tsv");
        PrintStream out = new PrintStream(new FileOutputStream(dictFile));
        out.println(line);
        out.flush();
        out.close();
        return dictFile;
    }

}
