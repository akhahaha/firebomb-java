package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public class OneToManyDefinition extends RelationDefinition {
    private String foreignFieldName;

    public OneToManyDefinition(String name, Field field,
                               BasicEntityDefinition foreignEntityDefinition, String foreignFieldName) {
        super(name, field, foreignEntityDefinition);
        this.foreignFieldName = foreignFieldName;
    }

    public OneToManyDefinition(String name, Method getMethod, Method setMethod,
                               BasicEntityDefinition foreignEntityDefinition, String foreignFieldName) {
        super(name, getMethod, setMethod, foreignEntityDefinition);
        this.foreignFieldName = foreignFieldName;
    }

    @Override
    public Collection get(Object entity) {
        Collection collection = (Collection) super.get(entity);
        return collection != null ? collection : Collections.emptyList();
    }

    public String getForeignFieldName() {
        return foreignFieldName;
    }

    public String constructForeignFieldPath(String foreignEntityId) {
        return path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignFieldName);
    }
}
