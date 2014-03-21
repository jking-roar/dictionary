package lang;

public class Definition {
    public final String word;
    public final String pos;
    public final String gloss;

    @Override
    public String toString() {
        return "Definition{" +
                "word='" + word + '\'' +
                ", gloss='" + gloss + '\'' +
                ", pos='" + pos + '\'' +
                '}';
    }

    public Definition(String word, String pos, String gloss) {
        this.word = word;
        this.pos = pos;
        this.gloss = gloss;
    }
}
