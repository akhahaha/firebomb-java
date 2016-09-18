package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class IdDefinition extends PropertyDefinition {
    private boolean isGeneratedValue;

    public IdDefinition(String name, Field field, boolean isNonNull) {
        super(name, field);
        this.isGeneratedValue = isNonNull;
    }

    public IdDefinition(String name, Method getMethod, Method setMethod, boolean isNonNull) {
        super(name, getMethod, setMethod);
        this.isGeneratedValue = isNonNull;
    }

    @Override
    public String get(Object entity) {
        return (String) super.get(entity);
    }

    public boolean isGeneratedValue() {
        return isGeneratedValue;
    }
}
