package firebomb.definition;

import firebomb.annotation.Id;
import firebomb.annotation.ManyToMany;
import firebomb.annotation.ManyToOne;
import firebomb.annotation.OneToMany;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class EntityDefinition extends BasicEntityDefinition {
    private List<FieldDefinition> fieldDefinitions = new ArrayList<>();
    private List<ManyToManyDefinition> manyToManyDefinitions = new ArrayList<>();
    private List<ManyToOneDefinition> manyToOneDefinitions = new ArrayList<>();
    private List<OneToManyDefinition> oneToManyDefinitions = new ArrayList<>();

    public EntityDefinition(Class<?> entityType) throws DefinitionException {
        super(entityType);

        Set<String> propertyNames = new HashSet<>();
        for (PropertyDefinition property : getProperties()) {
            // Check for property
            if (property.isAnnotationPresent(ManyToMany.class)) {
                manyToManyDefinitions.add(new ManyToManyDefinition(property));
            } else if (property.isAnnotationPresent(ManyToOne.class)) {
                manyToOneDefinitions.add(new ManyToOneDefinition(property));
            } else if (property.isAnnotationPresent(OneToMany.class)) {
                oneToManyDefinitions.add(new OneToManyDefinition(property));
            } else if (!property.isAnnotationPresent(Id.class)) {
                // Id set in BasicEntityDefinition
                // Only fields should remain
                fieldDefinitions.add(new FieldDefinition(property));
            }

            // Ensure property name uniqueness
            if (property.getName() != null && propertyNames.contains(property.getName())) {
                throw new DefinitionException("Property '" + getName() + "." + property.getName() +
                        "' has multiple definitions.");
            } else {
                propertyNames.add(property.getName());
            }
        }
    }

    public List<FieldDefinition> getFieldDefinitions() {
        return fieldDefinitions;
    }

    public List<OneToManyDefinition> getOneToManyDefinitions() {
        return oneToManyDefinitions;
    }

    public List<ManyToOneDefinition> getManyToOneDefinitions() {
        return manyToOneDefinitions;
    }

    public List<ManyToManyDefinition> getManyToManyDefinitions() {
        return manyToManyDefinitions;
    }

    private Class getGenericParameterClass(Type type) {
        if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length == 1) {
            return (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        return null;
    }

    public static Map<String, Object> getFieldMap(Object object) {
        EntityDefinition entityDefinition = new EntityDefinition(object.getClass());
        Map<String, Object> writeMap = new HashMap<>();

        // Write entity
        for (FieldDefinition fieldDefinition : entityDefinition.getFieldDefinitions()) {
            writeMap.put(fieldDefinition.getName(), fieldDefinition.get(object));
        }

        return writeMap;
    }
}
