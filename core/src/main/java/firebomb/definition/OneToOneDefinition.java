package firebomb.definition;

import firebomb.annotation.OneToOne;
import firebomb.util.StringUtils;

public class OneToOneDefinition extends RelationDefinition {
    private BasicEntityDefinition foreignEntityDefinition;
    private String foreignFieldName;

    public OneToOneDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        if (!isAnnotationPresent(OneToOne.class)) {
            throw new DefinitionException("OneToOne property '" + getEntityName() + "." + getName() +
                    "'missing OneToOne annotation.");
        }

        foreignEntityDefinition = new BasicEntityDefinition(getType());
        foreignFieldName = getAnnotation(OneToOne.class).foreignFieldName();
    }

    @Override
    public BasicEntityDefinition getForeignEntityDefinition() {
        return foreignEntityDefinition;
    }

    public String getForeignFieldName() {
        return foreignFieldName;
    }

    public String constructForeignIndexPath(String foreignEntityId) {
        return StringUtils.path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignFieldName);
    }
}
