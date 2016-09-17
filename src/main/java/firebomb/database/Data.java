package firebomb.database;

import java.util.List;

/**
 * Firebase DataSnapshot wrapper interface
 */
public interface Data {
    String getKey();

    Object getValue();

    <T> T getValue(Class<T> valueType);

    Data child(String path);

    List<Data> getChildren();
}
