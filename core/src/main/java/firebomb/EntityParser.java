package firebomb;

import firebomb.database.Data;

public interface EntityParser {
    <T> T deserialize(Class<T> entityType, Data data);

    Data serialize(Object entity);
}
