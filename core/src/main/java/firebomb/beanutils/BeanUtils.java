package firebomb.beanutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanUtils {
    public static boolean hasDefaultConstructor(Class classType) {
        if (classType.getConstructors().length == 0) {
            return true;
        }

        for (Constructor constructor : classType.getConstructors()) {
            if (constructor.getParameterTypes().length == 0) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGetter(Method method) {
        if (Modifier.isPublic(method.getModifiers()) &&
                method.getParameterTypes().length == 0) {
            if (method.getName().matches("^get[A-Z].*") && !method.getReturnType().equals(void.class))
                return true;
            if (method.getName().matches("^is[A-Z].*") && method.getReturnType().equals(boolean.class))
                return true;
        }

        return false;
    }

    public static boolean isSetter(Method method) {
        return Modifier.isPublic(method.getModifiers()) &&
                method.getReturnType().equals(void.class) &&
                method.getParameterTypes().length == 1 &&
                method.getName().matches("^set[A-Z].*");
    }

    public static String extractPropertyName(String accessorName) {
        String name = null;
        if (accessorName.matches("^get[A-Z].*") || accessorName.matches("^set[A-Z].*")) {
            name = accessorName.substring(3);
        } else if (accessorName.matches("^is[A-Z].*")) {
            name = accessorName.substring(2);
        }

        if (name != null && !name.isEmpty()) {
            char c[] = name.toCharArray();
            c[0] = Character.toLowerCase(c[0]);
            return new String(c);
        }

        return null;
    }

    public static List<BeanProperty> extractBeanProperties(Class classType) {
        List<BeanProperty> properties = new ArrayList<>();

        Map<String, Method> getters = new HashMap<>();
        // Find getters
        for (Method method : classType.getMethods()) {
            if (isGetter(method)) {
                getters.put(extractPropertyName(method.getName()), method);
            }
        }

        // Match setters
        for (Method method : classType.getMethods()) {
            if (isSetter(method)) {
                String name = extractPropertyName(method.getName());
                if (getters.containsKey(name)) {
                    Method getter = getters.get(name);
                    // Verify types
                    if (method.getParameterTypes()[0].equals(getter.getReturnType())) {
                        properties.add(new BeanProperty(getter, method));
                    }
                }
            }
        }

        return properties;
    }
}
