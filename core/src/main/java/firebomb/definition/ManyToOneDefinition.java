package firebomb.definition;

import firebomb.annotation.ManyToOne;
import firebomb.util.StringUtils;

public class ManyToOneDefinition extends RelationDefinition {
    private BasicEntityDefinition foreignEntityDefinition;
    private String foreignIndexName;

    public ManyToOneDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        if (!isAnnotationPresent(ManyToOne.class)) {
            throw new DefinitionException("ManyToOne property '" + getEntityName() + "." + getName() +
                    "'missing ManyToOne annotation.");
        }

        foreignEntityDefinition = new BasicEntityDefinition(getType());
        foreignIndexName = getAnnotation(ManyToOne.class).foreignIndexName();
    }

    @Override
    public BasicEntityDefinition getForeignEntityDefinition() {
        return foreignEntityDefinition;
    }

    public String getForeignIndexName() {
        return foreignIndexName;
    }

    public String constructForeignIndexPath(String foreignEntityId) {
        return StringUtils.path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignIndexName);
    }
}
