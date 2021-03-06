package dict;

import lang.Definition;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WiktionaryDefinitionsReaderTest {
    @Test
    public void readFileWithDefinitions() {
        File file = fileWithOneLine("English\tabend\tVerb\t# To terminate abnormally.");
        Reader reader = new WiktionaryDefinitionsReader(file.getAbsolutePath());
        List<Definition> defs = reader.getDefinitions("abend");
        assertEquals(1, defs.size());

        Definition d = defs.get(0);
        assertEquals("abend", d.word);
        assertEquals("To terminate abnormally.", d.meaning);
        assertEquals("Verb", d.pos);
    }

    @Test
    public void dropsSquareBrackets() {
        File file = fileWithOneLine("English\tabelsonite\tNoun\t# A [[mineral]].");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("abelsonite").get(0);
        assertEquals("A mineral.", def.meaning);
    }

    @Test
    public void dropEtymologies() {
        File file = fileWithOneLine("English\twarm\tEtymology 1\t# {{etyl|ine-pro|en}} {{term/t|ine-pro|*gʷʰer-}} (warm, hot), related to Ancient Greek {{term|θερμός|tr=thermos|lang=grc|sc=polytonic}}, Latin {{term|formus|lang=la}}, Sanskrit {{term|घर्म|tr=gharma|lang=sa|sc=Deva}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        assertEquals(0, reader.getDefinitions("warm").size());
    }

    @Test
    public void retrievesCorrectStringFromBrackets() {
        File file = fileWithOneLine("English\tword\tNoun\t# [[realWord|visible word]].");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("visible word.", def.meaning);

    }

    @Test
    public void dropsMarkup() {
        File file = fileWithOneLine("English\tabelsonite\tNoun\t# A [[mineral]], NiC<sub>31</sub>H<sub>32</sub>N<sub>4</sub>, forming [[soft]] reddish-brown [[triclinic]] crystals.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("abelsonite").get(0);
        assertEquals("A mineral, NiC31H32N4, forming soft reddish-brown triclinic crystals.", def.meaning);
    }

    @Test
    public void dropsCurlyBracesTwoParts() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{present participle of|divvy}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("present participle of divvy", def.meaning);
    }

    @Test
    public void dropContexts() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|computing|software|lang=en}} Microsoft Access Database");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Microsoft Access Database", def.meaning);
    }

    @Test
    public void braceAroundPunctuation() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{non-meaning definition|A title that can be used instead of the formal terms of [[marchioness]], [[countess]], [[viscountess]]{{,}} or [[baroness]].}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("A title that can be used instead of the formal terms of marchioness, countess, viscountess, or baroness.", def.meaning);
    }

    @Test
    public void formOf() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|obsolete|lang=en}} {{form of|past plural form|go|lang=en}}; [[went]]");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("past plural form of go; went", def.meaning);
    }

    @Test
    public void expandLatinDef() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|archaic|lang=en}} {{l|en|long s}} {{Latn-def|en|letter|19|long s|medial s|descending s}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("long s The letter of the Latin-script letter 19 long s medial s descending s.", def.meaning);
    }

    @Test
    public void alternativeLinkText() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{l|en|divination|Divination}} using {{l|en|wine}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Divination using wine.", def.meaning);
    }

    @Test
    public void plurals() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{plural of|word|lang=en}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("plural of word", def.meaning);

    }

    @Test
    public void dropCitations() {
        File file = fileWithOneLine("English\tword\tNoun\t# Something. <ref>{{cite web|url=http://somewhere.com|title=something|work=work|accessdate=2011-02-20}}</ref>");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Something.", def.meaning);
    }

    @Test
    public void nongloss() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{non-gloss definition|Either of two letters, little yus ({{l|mul|sc=Cyrs|Ѧ}}) and big yus ({{l|mul|sc=Cyrs|Ѫ}}), representing [[nasal]] vowel sounds in the [[Cyrillic]] alphabet. The only major [[Slavic]] language retaining these sounds is [[Polish]], which is written in the Latin alphabet.<!--Modern languages thus have little yus for them.-->}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Either of two letters, little yus (Ѧ) and big yus (Ѫ), representing nasal vowel sounds in the Cyrillic alphabet. The only major Slavic language retaining these sounds is Polish, which is written in the Latin alphabet.", def.meaning);
    }

    @Test
    public void nonglossWithReference() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{non-gloss definition|Airport code for [[Abadan]], [[Iran]].<ref>{{reference-book | last =| first = | authorlink =  | coauthors =  | editor =Morris, William    | others =  | title = The American Heritage Dictionary of the English Language | origdate =  | origyear = 1969| origmonth =  | url =  | format =  | accessdate =  | accessyear =  | accessmonth =  | edition =  | date =  | year =1971| month =  | publisher =American Heritage Publishing Co., Inc. | location = New York, NY | language =  | id =  | doi = | isbn =0-395-09066-0 | lccn = | ol = | pages =1| chapter =  | chapterurl =  | quote =}}</ref>}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Airport code for Abadan, Iran.", def.meaning);
    }

    @Test
    public void alternativeForm() {
        File file = fileWithOneLine("English\tword\tNoun\t{{alternative form of|apheresis}} {{qualifier|loss of a letter or sound from the beginning of a word}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("alternative form of apheresis loss of a letter or sound from the beginning of a word", def.meaning);
    }

    @Test
    public void wot() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{conjugation of|wit||1|s|pres|ind|lang=en}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("conjugation of wit", def.meaning);
    }

    @Test
    public void substub() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{substub}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("substub", def.meaning);
    }

    @Test
    public void langLinks() {
        File file = fileWithOneLine("English\tword\tNoun\t# A cross composed of four {{l/en|triquetra|triquetrae}} which meet at the {{l/en|crux}} by their {{l/en|vertex|vertices}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("A cross composed of four triquetrae which meet at the crux by their vertices.", def.meaning);
    }

    @Test
    public void gcj() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|software|lang=en}} {{initialism of|[[GNU]] [[compiler|Compiler]] for [[Java]]}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("initialism of GNU Compiler for Java", def.meaning);
    }

    @Test
    public void zee() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|US|lang=en}} {{Latn-def|en|name|Z|z}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("The name of the Latin-script letter Z z.", def.meaning);
    }

    @Test
    public void citeWeb() {
        File file = fileWithOneLine("English\tword\tNoun\t# Wild animals, excluding fish.<ref name=\"wa\">{{cite web");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Wild animals, excluding fish.", def.meaning);
    }

    @Test
    public void offSignal() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|military|lang=en}} The [[staff]] of an [[army]], including all [[officer]]s above the rank of [[colonel]], all [[adjutant]]s, [[inspector]]s, [[quartermaster]]s, [[commissary|commissaries]], [[engineer]]s, [[ordnance]] officers, [[paymaster]]s, [[physician]]s, [[signal] officers, and judge advocates, and their [[noncommissioned]] [[assistant]]s.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("The staff of an army, including all officers above the rank of colonel, all adjutants, inspectors, quartermasters, commissaries, engineers, ordnance officers, paymasters, physicians, signal officers, and judge advocates, and their noncommissioned assistants.", def.meaning);
    }

    @Test
    public void were() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|archaic|lang=en}} [[man]] (human male), as in {{term|werewolf||man-wolf|lang=en}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("man (human male), as in werewolf (man-wolf).", def.meaning);
    }

    @Test
    public void refWithPreperties() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{archaic spelling of|equilibrium}}<ref name=\"OED-alt\">“[http://dictionary.oed.com/cgi/entry/50077253?sp=1 equilibrium]” in the '''Oxford English Dictionary''' (second edition), giving {{term||æquilibrium|lang=en}} as a 17th–19th-century spelling.</ref>");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("archaic spelling of equilibrium", def.meaning);
    }

    @Test
    public void andry() {
        File file = fileWithOneLine("English\tword\tNoun\t# male [[reproductive]] [[organ]](s); {{context|especially in|_|botany|lang=en}}: [[stamen]](s)");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("male reproductive organ(s); : stamen(s)", def.meaning);
    }

    @Test
    public void dropLabels() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{label|en|uncountable|slang|professional wrestling}} The [[staging]] of events to appear as real.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("The staging of events to appear as real.", def.meaning);
    }

    @Test
    public void dropLiteralTranslationsOfIdioms() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{&lit|writ|large|larger|largest}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        assertEquals(0, reader.getDefinitions("word").size());
    }

    @Test
    public void multipleContexts() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|Scotland|lang=en} and {context|South Africa|lang=en}} In a [[timely]] manner.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("In a timely manner.", def.meaning);
    }

    @Test
    public void needsDefinition() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{rfdef|mathematics|physics|medicine|at least possibly|lang=en}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        assertEquals(0, reader.getDefinitions("word").size());
    }

    @Test
    public void terameter() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{SI-unit-2|tera|metre|length|meter}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("An SI unit", def.meaning);
    }

    @Test
    public void phantasia() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{dated form of||fantasia}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("dated form of fantasia", def.meaning);
    }

    @Test
    public void referencesRemoved() {
        File file = fileWithOneLine("English\tword\tNoun\t# some text <ref>any reference text</ref> other text");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("some text other text", def.meaning);
    }

    @Test
    public void shakuhachi() {
        File file = fileWithOneLine("English\tword\tNoun\t# [...] Monks of the {{term|普化禅|||tr=[[ふけぜん]], [[fuke zen]]|sc=Jpan|lang=ja}} sect in the practice of {{term|吹禅||blowing meditation|tr=[[すいぜん]], [[suizen]]|lang=ja|sc=Jpan}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("[...] Monks of the ふけぜん, fuke zen sect in the practice of すいぜん, suizen.", def.meaning);
    }

    @Test
    public void translations() {
        File file = fileWithOneLine("English\tword\tNoun\t# [...] descended from the Latin {{term|lang=la|ipse||self}} instead of the Latin {{term|lang=la|ille||that}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("[...] descended from the Latin ipse (self) instead of the Latin ille (that).", def.meaning);
    }

    @Test
    public void translationPoblano() {
        File file = fileWithOneLine("English\tword\tNoun\tthe chilis are called [[ancho]]s or {{term|chile ancho|chiles anchos|wide chilis|lang=es}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("the chilis are called anchos or chiles anchos (wide chilis).", def.meaning);
    }

    @Test
    public void taxlink() {
        File file = fileWithOneLine("English\tword\tNoun\t# A southern African subspecies of [[zebra]], {{taxlink|Equus quagga quagga|subspecies||noshow=1}}, which went");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("A southern African subspecies of zebra, Equus quagga quagga, which went", def.meaning);
    }

    @Test
    public void words() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|legal|lang=en}} {{abbreviation of|{{w|United States Court of Appeals for the Tenth Circuit}}}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("abbreviation of United States Court of Appeals for the Tenth Circuit", def.meaning);
    }

    @Test
    public void soplink() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{eye dialect of|{{soplink|do|you|know|what|I|mean}}}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("eye dialect of do you know what I mean", def.meaning);
    }

    @Test
    public void kamora() {
        File file = fileWithOneLine("English\tword\tNoun\t# The diacritic {{l|mul|sc=Cyrs|҄|&nbsp;҄}}, used [...]");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("The diacritic &nbsp;҄, used [...]", def.meaning);
    }

    @Test
    public void jukujikun() {
        File file = fileWithOneLine("English\tword\tNoun\t# For example, {{term|lang=ja|大||big|pos=usually read ''ō'' in {{l|ja|sc=Latn|訓読み|''kun'yomi''}} compounds}} and");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("For example, 大 (\"big\", usually read ''ō'' in ''kun'yomi'' compounds) and", def.meaning);
    }

    @Test
    public void givenName() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{given name|female|from=Hebrew|}}; formerly rare");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("A given female name; formerly rare", def.meaning);
    }

    @Test
    public void surname() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{surname||from=Italian}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("A surname", def.meaning);
    }

    @Test
    public void twoPartW() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{context|US|lang=en}} {{initialism of|{{w|AFL-CIO|American Federation of Labor-Congress of Industrial Organizations}}}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("initialism of American Federation of Labor-Congress of Industrial Organizations", def.meaning);
    }

    @Test
    public void exprAndTerms() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{#expr:{{some term}}-1}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("XXXXXXX", def.meaning);
    }

    @Test
    public void gnuFdl() {
        File file = fileWithOneLine("English\tword\tNoun\t# {{initialism of|{{pedlink|GNU Free Documentation License}}}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("initialism of GNU Free Documentation License", def.meaning);
    }

    @Test
    public void daf() {
        File file = fileWithOneLine("English\tword\tNoun\t# Hebrew {{term|ברך|tr=barúkh|lang=he||blessed}}.");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Hebrew ברך (barúkh, blessed).", def.meaning);
    }

    @Test
    public void jumps() {
        File file = fileWithOneLine("English\tword\tNoun\t# Definition. {{jump|en|the word|u|s|t}}");
        Reader reader = new WiktionaryDefinitionsReader(file.getPath());
        Definition def = reader.getDefinitions("word").get(0);
        assertEquals("Definition.", def.meaning);
    }

    private File fileWithOneLine(String line) {
        try {
            File dictFile = File.createTempFile("wiktionary", "tsv");
            PrintStream out = new PrintStream(new FileOutputStream(dictFile), true, "UTF-8");
            out.println(line);
            out.flush();
            out.close();
            return dictFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
