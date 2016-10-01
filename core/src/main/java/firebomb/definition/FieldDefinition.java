package firebomb.definition;

import firebomb.annotation.NonNull;

public class FieldDefinition extends PropertyDefinition {
    private boolean isNonNull;

    public FieldDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        isNonNull = isAnnotationPresent(NonNull.class);
    }

    public boolean isNonNull() {
        return isNonNull;
    }
}
