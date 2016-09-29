package firebomb.beanutils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BeanProperty {
    private String name;
    private Method getter;
    private Method setter;
    private List<Annotation> annotations = new ArrayList<>();

    public BeanProperty(Method getter, Method setter) {
        this.getter = getter;
        this.setter = setter;

        if (!BeanUtils.isGetter(getter)) {
            throw new IllegalArgumentException("Invalid getter method.");
        }

        if (!BeanUtils.isSetter(setter)) {
            throw new IllegalArgumentException("Invalid setter method.");
        }

        name = BeanUtils.extractPropertyName(getter.getName());
        if (name == null || !name.equals(BeanUtils.extractPropertyName(setter.getName()))) {
            throw new IllegalArgumentException("Getter and setter name mismatch.");
        }

        if (!getter.getReturnType().equals(setter.getParameterTypes()[0])) {
            throw new IllegalArgumentException("Getter and setter type mismatch.");
        }

        if (!getter.getDeclaringClass().equals(setter.getDeclaringClass())) {
            throw new IllegalArgumentException("Getter and setter declaring class mismatch.");
        }

        annotations.addAll(Arrays.asList(getter.getAnnotations()));
        annotations.addAll(Arrays.asList(setter.getAnnotations()));
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return getter.getReturnType();
    }

    public Type getGenericType() {
        return getter.getGenericReturnType();
    }

    public Object get(Object bean) throws InvocationTargetException, IllegalAccessException {
        return getter.invoke(bean);
    }

    public void set(Object bean, Object value) throws InvocationTargetException, IllegalAccessException {
        setter.invoke(bean, value);
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
}
