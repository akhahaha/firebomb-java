package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class PropertyDefinition {
    private String name;
    private Field field;
    private Method getMethod;
    private Method setMethod;

    public PropertyDefinition(String name, Field field) {
        this.name = name;
        this.field = field;
    }

    public PropertyDefinition(String name, Method getMethod, Method setMethod) {
        this.name = name;
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }

    public String getName() {
        return name;
    }

    public Object get(Object entity) {
        Object value;
        try {
            if (field != null) {
                value = field.get(entity);
            } else {
                value = getMethod.invoke(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(e);
        }

        return value;
    }

    public void set(Object entity, Object value) {
        try {
            if (field != null) {
                field.set(entity, value);
            } else {
                setMethod.invoke(entity, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(e);
        }
    }

    public Class<?> getType() {
        if (field != null) {
            return field.getType();
        } else {
            return getMethod.getReturnType();
        }
    }

    protected static String path(String... nodes) {
        return String.join("/", (CharSequence[]) nodes);
    }
}
