package firebomb.definition;

public abstract class RelationDefinition extends PropertyDefinition {
    public RelationDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
    }

    public abstract BasicEntityDefinition getForeignEntityDefinition();

    public Class<?> getForeignEntityType() {
        return getForeignEntityDefinition().getEntityType();
    }

    public String getForeignIdName() {
        return getForeignEntityDefinition().getIdName();
    }

    public String getForeignId(Object foreignEntity) {
        return getForeignEntityDefinition().getId(foreignEntity);
    }

    public void setForeignId(Object foreignEntity, String value) {
        getForeignEntityDefinition().setId(foreignEntity, value);
    }
}
