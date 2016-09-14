package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ManyToOneDefinition extends RelationDefinition {
    private String foreignIndexName;

    public ManyToOneDefinition(String name, Field field,
                               BasicEntityDefinition foreignEntityDefinition, String foreignIndexName) {
        super(name, field, foreignEntityDefinition);
        this.foreignIndexName = foreignIndexName;
    }

    public ManyToOneDefinition(String name, Method getMethod, Method setMethod,
                               BasicEntityDefinition foreignEntityDefinition, String foreignIndexName) {
        super(name, getMethod, setMethod, foreignEntityDefinition);
        this.foreignIndexName = foreignIndexName;
    }

    public String getForeignIndexName() {
        return foreignIndexName;
    }

    public String constructForeignIndexPath(String foreignEntityId) {
        return path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignIndexName);
    }
}
