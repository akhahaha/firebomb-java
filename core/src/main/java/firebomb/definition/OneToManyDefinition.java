package firebomb.definition;

import firebomb.annotation.OneToMany;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;

public class OneToManyDefinition extends RelationDefinition {
    private BasicEntityDefinition foreignEntityDefinition;
    private String foreignFieldName;

    public OneToManyDefinition(PropertyDefinition propertyDefinition) {
        super(propertyDefinition);
        initialize();
    }

    private void initialize() {
        if (!isAnnotationPresent(OneToMany.class)) {
            throw new DefinitionException("OneToMany property '" + getEntityName() + "." + getName() +
                    "'missing OneToMany annotation.");
        }

        if (!Collection.class.isAssignableFrom(getType())) {
            throw new DefinitionException("OneToMany property '" + getEntityName() + "." + getName() +
                    "'must extend Collection.");
        }

        Class genericClass = getGenericParameterClass(getGenericType());
        if (genericClass == null) {
            throw new DefinitionException("Unable to resolve generic type parameter for '" +
                    getEntityName() + "." + getName() + "'.");
        }

        foreignEntityDefinition = new BasicEntityDefinition(genericClass);
        foreignFieldName = getAnnotation(OneToMany.class).foreignFieldName();
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

    public String getForeignFieldName() {
        return foreignFieldName;
    }

    public String constructForeignFieldPath(String foreignEntityId) {
        return path(getForeignEntityDefinition().getReference(), foreignEntityId, foreignFieldName);
    }

    private Class getGenericParameterClass(Type type) {
        if (type instanceof ParameterizedType && ((ParameterizedType) type).getActualTypeArguments().length == 1) {
            return (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
        }

        return null;
    }
}
