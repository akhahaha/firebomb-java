package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class RelationDefinition extends PropertyDefinition {
    private BasicEntityDefinition foreignEntityDefinition;

    public RelationDefinition(String name, Field field,
                              BasicEntityDefinition foreignEntityDefinition) {
        super(name, field);
        this.foreignEntityDefinition = foreignEntityDefinition;
    }

    public RelationDefinition(String name, Method getMethod, Method setMethod,
                              BasicEntityDefinition foreignEntityDefinition) {
        super(name, getMethod, setMethod);
        this.foreignEntityDefinition = foreignEntityDefinition;
    }

    public BasicEntityDefinition getForeignEntityDefinition() {
        return foreignEntityDefinition;
    }

    public Class<?> getForeignEntityType() {
        return foreignEntityDefinition.getEntityType();
    }

    public String getForeignIdName() {
        return foreignEntityDefinition.getIdName();
    }

    public String getForeignId(Object foreignEntity) {
        return foreignEntityDefinition.getId(foreignEntity);
    }

    public void setForeignId(Object foreignEntity, String value) {
        foreignEntityDefinition.setId(foreignEntity, value);
    }
}
