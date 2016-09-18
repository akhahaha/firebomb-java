package firebomb.definition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldDefinition extends PropertyDefinition {
    private boolean isNonNull;

    public FieldDefinition(String name, Field field, boolean isNonNull) {
        super(name, field);
        this.isNonNull = isNonNull;
    }

    public FieldDefinition(String name, Method getMethod, Method setMethod, boolean isNonNull) {
        super(name, getMethod, setMethod);
        this.isNonNull = isNonNull;
    }

    public boolean isNonNull() {
        return isNonNull;
    }
}
