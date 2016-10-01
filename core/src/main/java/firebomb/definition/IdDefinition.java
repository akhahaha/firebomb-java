package firebomb.definition;

import firebomb.annotation.GeneratedValue;
import firebomb.annotation.Id;

public class IdDefinition extends PropertyDefinition {
    private boolean isGeneratedValue;

    public IdDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        if (!isAnnotationPresent(Id.class)) {
            throw new DefinitionException("Id property '" + getEntityName() + "." + getName() +
                    "'missing Id annotation.");
        }

        // Verify is String
        if (!String.class.isAssignableFrom(getType())) {
            throw new DefinitionException("Id property '" + getEntityName() + "." + getName() + "' " +
                    "must extend String.");
        }

        isGeneratedValue = isAnnotationPresent(GeneratedValue.class);
    }

    @Override
    public String get(Object entity) {
        return (String) super.get(entity);
    }

    public boolean isGeneratedValue() {
        return isGeneratedValue;
    }
}
