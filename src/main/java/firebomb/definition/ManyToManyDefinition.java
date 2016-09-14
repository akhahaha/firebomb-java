package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

public class ManyToManyDefinition extends RelationDefinition {
    private String foreignIndexName;

    public ManyToManyDefinition(String name, Field field,
                                BasicEntityDefinition foreignEntityDefinition, String foreignIndexName) {
        super(name, field, foreignEntityDefinition);
        this.foreignIndexName = foreignIndexName;
    }

    public ManyToManyDefinition(String name, Method getMethod, Method setMethod,
                                BasicEntityDefinition foreignEntityDefinition, String foreignIndexName) {
        super(name, getMethod, setMethod, foreignEntityDefinition);
        this.foreignIndexName = foreignIndexName;
    }

    @Override
    public Collection get(Object entity) {
        Collection collection = (Collection) super.get(entity);
        return collection != null ? collection : Collections.emptyList();
    }

    public String getForeignIndexName() {
        return foreignIndexName;
    }

    public String constructForeignIndexPath(String foreignEntityId) {
        return path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignIndexName);
    }
}
