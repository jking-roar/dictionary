package dict;

import lang.Definition;

import java.util.List;

public interface Reader {
    List<Definition> getDefinitions(String word);
    java.util.Set<Definition> allDefinitions();
}
