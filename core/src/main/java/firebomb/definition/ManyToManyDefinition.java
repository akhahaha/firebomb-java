package firebomb.definition;

import firebomb.annotation.ManyToMany;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

public class ManyToManyDefinition extends RelationDefinition {
    private BasicEntityDefinition foreignEntityDefinition;
    private String foreignIndexName;

    public ManyToManyDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        if (!isAnnotationPresent(ManyToMany.class)) {
            throw new DefinitionException("ManyToMany property '" + getEntityName() + "." + getName() +
                    "' missing ManyToMany annotation.");
        }

        if (!Collection.class.isAssignableFrom(getType())) {
            throw new DefinitionException("ManyToMany property '" + getEntityName() + "." + getName() +
                    "'must extend Collection.");
        }

        Class genericClass = getGenericParameterClass(getGenericType());
        if (genericClass == null) {
            throw new DefinitionException("Unable to resolve generic type parameter for '" +
                    getEntityName() + "." + getName() + "'.");
        }

        foreignEntityDefinition = EntityDefinitionManager.getInstance().getBasicDefinition(genericClass);
        foreignIndexName = getAnnotation(ManyToMany.class).foreignIndexName();
    }

    @Override
    public Collection get(Object entity) {
        Collection collection = (Collection) super.get(entity);
        return collection != null ? collection : Collections.emptyList();
    }

    @Override
    public BasicEntityDefinition getForeignEntityDefinition() {
        return foreignEntityDefinition;
    }

    public String getForeignIndexName() {
        return foreignIndexName;
    }

    public String constructForeignIndexPath(String foreignEntityId) {
        return path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignIndexName);
    }

    private Class getGenericParameterClass(Type type) {
        if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length == 1) {
            return (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        return null;
    }
}
