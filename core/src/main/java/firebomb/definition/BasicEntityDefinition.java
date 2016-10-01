package firebomb.definition;

import firebomb.annotation.*;
import firebomb.beanutils.BeanProperty;
import firebomb.beanutils.BeanUtils;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class BasicEntityDefinition {
    private Class<?> entityType;
    private String name;
    private String reference;
    private IdDefinition idDefinition;
    private List<PropertyDefinition> properties = new ArrayList<>();

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
        if (!BeanUtils.hasDefaultConstructor(entityType)) {
            throw new DefinitionException("Entity '" + name + "' requires default constructor.");
        }

        // Get properties
        // Get field properties
        for (java.lang.reflect.Field field : entityType.getFields()) {
            int modifiers = field.getModifiers();

            // Ignore private, static, and @Ignored fields
            if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) ||
                    field.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            properties.add(new PropertyDefinition(name, field));
        }

        // Get bean properties
        for (BeanProperty beanProperty : BeanUtils.extractBeanProperties(entityType)) {
            if (beanProperty.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            properties.add(new PropertyDefinition(name, beanProperty));
        }

        // Find Id
        for (PropertyDefinition property : getProperties()) {
            if (property.isAnnotationPresent(Id.class)) {
                // Verify no duplicate Ids
                if (this.idDefinition != null) {
                    throw new DefinitionException("Duplicate Id property found for entity '" + this.name + "'.");
                }

                this.idDefinition = new IdDefinition(property);
            }
        }

        if (idDefinition == null) {
            throw new DefinitionException("Id property required for entity '" + getName() + "'.");
        }
    }

    private void inspectBasicFields() {
        for (java.lang.reflect.Field field : entityType.getFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers)) {
                continue;
            }

            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }


        }
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

    protected List<PropertyDefinition> getProperties() {
        return properties;
    }
}
