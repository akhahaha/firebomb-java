package firebomb.definition;

import firebomb.annotation.*;

import java.lang.reflect.Modifier;
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

        // Get properties
        Set<String> propertyNames = new HashSet<>();

        // Inspect fields
        for (java.lang.reflect.Field field : entityType.getFields()) {
            if (field.isAnnotationPresent(Ignore.class)) {
                continue;
            }

            String name = field.getName();
            if (field.isAnnotationPresent(Property.class)) {
                name = field.getAnnotation(Property.class).value();
            }

            // Check property
            int fieldPropertyDefinitionCount = 0;
            if (field.isAnnotationPresent(ManyToMany.class)) {
                String foreignIndexName = field.getAnnotation(ManyToMany.class).foreignIndexName();

                // Verify is collection
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    throw new DefinitionException("ManyToOne property '" + getName() + "." + name + "' " +
                            "must extend Collection");
                }

                Class foreignEntityType = getGenericParameterClass(field.getGenericType());
                if (foreignEntityType == null) {
                    throw new DefinitionException("ManyToOne property '" + getName() + "." + name + "' " +
                            "unable to resolve generic type parameter.");
                }

                manyToManyDefinitions.add(new ManyToManyDefinition(name, field,
                        new BasicEntityDefinition(foreignEntityType), foreignIndexName));
                fieldPropertyDefinitionCount++;
            }

            if (field.isAnnotationPresent(ManyToOne.class)) {
                String foreignIndexName = field.getAnnotation(ManyToOne.class).foreignIndexName();
                Class foreignEntityType = field.getType();
                manyToOneDefinitions.add(new ManyToOneDefinition(name, field,
                        new BasicEntityDefinition(foreignEntityType), foreignIndexName));
                fieldPropertyDefinitionCount++;
            }

            if (field.isAnnotationPresent(OneToMany.class)) {
                String foreignFieldName = field.getAnnotation(OneToMany.class).foreignFieldName();

                // Verify is collection
                if (!Collection.class.isAssignableFrom(field.getType())) {
                    throw new DefinitionException("ManyToOne property '" + getName() + "." + name + "' " +
                            "must extend Collection");
                }

                Class foreignEntityType = getGenericParameterClass(field.getGenericType());
                if (foreignEntityType == null) {
                    throw new DefinitionException("ManyToOne property '" + getName() + "." + name + "' " +
                            "unable to resolve generic type parameter.");
                }

                oneToManyDefinitions.add(new OneToManyDefinition(name, field,
                        new BasicEntityDefinition(foreignEntityType), foreignFieldName));
                fieldPropertyDefinitionCount++;
            }

            if (field.isAnnotationPresent(Id.class)) {
                // Id set in super()
                fieldPropertyDefinitionCount++;
            }

            if (field.isAnnotationPresent(Field.class)) {
                // Private fields
                fieldDefinitions.add(new FieldDefinition(name, field, field.isAnnotationPresent(NonNull.class)));
                fieldPropertyDefinitionCount++;
            }

            if (fieldPropertyDefinitionCount == 0 && Modifier.isPublic(field.getModifiers())) {
                // Public fields
                fieldDefinitions.add(new FieldDefinition(name, field, field.isAnnotationPresent(NonNull.class)));
                fieldPropertyDefinitionCount++;
            }

            // Mutually exclude property definitions and ensure property name uniqueness
            if (fieldPropertyDefinitionCount > 1 || (name != null && propertyNames.contains(name))) {
                throw new DefinitionException("Property '" + getName() + "." + name + "' has multiple definitions.");
            } else if (fieldPropertyDefinitionCount > 0) {
                propertyNames.add(name);
            }
        }

        // TODO: Inspect methods
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
