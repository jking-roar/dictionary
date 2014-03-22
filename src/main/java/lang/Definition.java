package lang;

public class Definition {
    public final String word;
    public final String pos;
    public final String meaning;

    @Override
    public String toString() {
        return "Definition{" +
                "word='" + word + '\'' +
                ", meaning='" + meaning + '\'' +
                ", pos='" + pos + '\'' +
                '}';
    }

    public Definition(String word, String pos, String meaning) {
        this.word = word;
        this.pos = pos;
        this.meaning = meaning;
    }
}
