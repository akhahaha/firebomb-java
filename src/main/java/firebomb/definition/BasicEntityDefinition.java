package firebomb.definition;

import firebomb.annotation.*;

import java.lang.reflect.Constructor;

public class BasicEntityDefinition {
    private Class<?> entityType;
    private String name;
    private String reference;
    private IdDefinition idDefinition;

    public BasicEntityDefinition(Class<?> entityType) throws DefinitionException {
        this.entityType = entityType;

        // Get name
        this.name = entityType.getSimpleName();
        if (entityType.isAnnotationPresent(Property.class)) {
            this.name = entityType.getAnnotation(Property.class).value();
        }

        // Get reference
        this.reference = name.toLowerCase() + "s"; // Infer reference name from entity name
        if (entityType.isAnnotationPresent(Reference.class)) {
            this.reference = entityType.getAnnotation(Reference.class).value();
        }

        if (!entityType.isAnnotationPresent(Entity.class)) {
            throw new DefinitionException("Object '" + name + "' not annotated as Entity.");
        }

        // Verify default constructor present
        boolean hasDefaultConstructor = false;
        for (Constructor constructor : entityType.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                hasDefaultConstructor = true;
                break;
            }
        }
        if (!hasDefaultConstructor) {
            throw new DefinitionException("Entity '" + name + "' missing default constructor.");
        }

        // Inspect fields
        for (java.lang.reflect.Field field : entityType.getFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                String name = field.getName();
                if (field.isAnnotationPresent(Property.class)) {
                    name = field.getAnnotation(Property.class).value();
                }

                // Verify no duplicate Ids
                if (this.idDefinition != null) {
                    throw new DefinitionException("Duplicate Id property found for '" + this.name + "'.");
                }

                // Verify is String
                if (!String.class.isAssignableFrom(field.getType())) {
                    throw new DefinitionException("Id property '" + getName() + "." + name + "' " +
                            "must extend String");
                }

                this.idDefinition = new IdDefinition(name, field, field.isAnnotationPresent(GeneratedValue.class));
            }
        }

        if (idDefinition == null) {
            throw new DefinitionException("Id property required for '" + getName() + "." + name + "'");
        }

        // TODO: Inspect methods
    }

    public Class getEntityType() {
        return entityType;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public IdDefinition getIdDefinition() {
        return idDefinition;
    }

    public String getIdName() {
        return idDefinition.getName();
    }

    public String getId(Object entity) {
        return idDefinition.get(entity);
    }

    public void setId(Object entity, String value) {
        idDefinition.set(entity, value);
    }
}
