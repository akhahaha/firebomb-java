package firebomb.definition;

import firebomb.annotation.*;
import firebomb.beanutils.BeanProperty;
import firebomb.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class PropertyDefinition {
    private String entityName;
    private String name;
    private Field field;
    private BeanProperty property;
    private List<Annotation> annotations;

    public PropertyDefinition(String entityName, Field field) {
        initialize(entityName, field);
    }

    public PropertyDefinition(String entityName, BeanProperty property) {
        initialize(entityName, property);
    }

    protected PropertyDefinition(PropertyDefinition propertyDefinition) {
        if (propertyDefinition.isField()) {
            initialize(propertyDefinition.getEntityName(), propertyDefinition.getField());
        } else {
            initialize(propertyDefinition.getEntityName(), propertyDefinition.getBeanProperty());
        }
    }

    private void initialize(String entityName, Field field) {
        this.entityName = entityName;
        this.name = field.getName();
        this.field = field;
        this.annotations = Arrays.asList(field.getAnnotations());
        processAnnotations();
    }

    private void initialize(String entityName, BeanProperty property) {
        this.entityName = entityName;
        this.name = property.getName();
        this.property = property;
        this.annotations = property.getAnnotations();
        processAnnotations();
    }

    public String getEntityName() {
        return entityName;
    }

    public String getName() {
        return name;
    }

    public boolean isField() {
        return field != null;
    }

    public Field getField() {
        return field;
    }

    public boolean isBeanProperty() {
        return property != null;
    }

    public BeanProperty getBeanProperty() {
        return property;
    }

    public Class<?> getType() {
        if (field != null) {
            return field.getType();
        } else {
            return property.getType();
        }
    }

    public Type getGenericType() {
        if (field != null) {
            return field.getGenericType();
        } else {
            return property.getGenericType();
        }
    }

    public Object get(Object entity) {
        Object value;
        try {
            if (field != null) {
                value = field.get(entity);
            } else {
                value = property.get(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(e);
        }

        return value;
    }

    public void set(Object entity, Object value) {
        try {
            if (field != null) {
                field.set(entity, value);
            } else {
                property.set(entity, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DefinitionException(e);
        }
    }

    protected static String path(String... nodes) {
        return StringUtils.join("/", nodes);
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return true;
            }
        }

        return false;
    }

    public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().equals(annotationType)) {
                return (T) annotation;
            }
        }

        return null;
    }

    private void processAnnotations() {
        if (isAnnotationPresent(Property.class)) {
            this.name = getAnnotation(Property.class).value();
        }

        boolean oneOf = false;
        boolean hasId = false;
        boolean hasGeneratedValue = false;
        boolean hasField = false;
        boolean hasNonNull = false;

        /*
          Rules:
          - Only one of: Field, Id, ManyToMany, ManyToOne, OneToMany
          - GeneratedValue only with Id
          - NonNull only with Field
         */
        for (Annotation annotation : annotations) {
            if (annotation instanceof firebomb.annotation.Field ||
                    annotation instanceof Id ||
                    annotation instanceof ManyToMany ||
                    annotation instanceof ManyToOne ||
                    annotation instanceof OneToMany) {
                if (oneOf) {
                    throw new DefinitionException("Multiple property types defined for '" +
                            getEntityName() + "." + getName() + "'.");
                }

                oneOf = true;
            }

            if (annotation instanceof Id) {
                hasId = true;
            } else if (annotation instanceof GeneratedValue) {
                hasGeneratedValue = true;
            } else if (annotation instanceof firebomb.annotation.Field) {
                hasField = true;
            } else if (annotation instanceof NonNull) {
                hasNonNull = true;
            }
        }

        if (hasGeneratedValue && !hasId) {
            throw new DefinitionException("@GeneratedValue defined found for non-Id property '" +
                    getEntityName() + "." + getName() + "'.");
        }

        if (hasNonNull && (!oneOf || hasField)) {
            throw new DefinitionException("@GeneratedValue defined found for non-Field property '" +
                    getEntityName() + "." + getName() + "'.");
        }
    }
}
