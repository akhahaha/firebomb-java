package firebomb.definition;

import firebomb.annotation.ManyToMany;
import firebomb.annotation.ManyToOne;
import firebomb.annotation.OneToMany;
import firebomb.annotation.OneToOne;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityDefinition extends BasicEntityDefinition {
    private List<ManyToManyDefinition> manyToManyDefinitions = new ArrayList<>();
    private List<ManyToOneDefinition> manyToOneDefinitions = new ArrayList<>();
    private List<OneToManyDefinition> oneToManyDefinitions = new ArrayList<>();
    private List<OneToOneDefinition> oneToOneDefinitions = new ArrayList<>();

    public EntityDefinition(Class<?> entityType) throws DefinitionException {
        super(entityType);

        for (PropertyDefinition property : getProperties()) {
            // Check for property
            if (property.isAnnotationPresent(ManyToMany.class)) {
                manyToManyDefinitions.add(new ManyToManyDefinition(property));
            } else if (property.isAnnotationPresent(ManyToOne.class)) {
                manyToOneDefinitions.add(new ManyToOneDefinition(property));
            } else if (property.isAnnotationPresent(OneToMany.class)) {
                oneToManyDefinitions.add(new OneToManyDefinition(property));
            } else if (property.isAnnotationPresent(OneToOne.class)) {
                oneToOneDefinitions.add(new OneToOneDefinition(property));
            }
        }
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

    public List<OneToOneDefinition> getOneToOneDefinitions() {
        return oneToOneDefinitions;
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
