package firebomb.beanutils;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BeanUtilsTest {
    @Test
    public void hasDefaultConstructor() throws Exception {
        assert BeanUtils.hasDefaultConstructor(TestBean.class);
        assert BeanUtils.hasDefaultConstructor(TestBean2.class);
        assert !BeanUtils.hasDefaultConstructor(TestBean3.class);
    }

    @Test
    public void extractBeanProperties() throws Exception {
        List<BeanProperty> properties = BeanUtils.extractBeanProperties(TestBean.class);
        assertEquals(properties.size(), 1);
        assertEquals(properties.get(0).getName(), "property");
    }

    @Test
    public void isGetter() throws Exception {
        Set<String> methodNames = new HashSet<>();
        for (Method method : TestBean.class.getDeclaredMethods()) {
            if (BeanUtils.isGetter(method)) {
                methodNames.add(method.getName());
            }
        }

        assertEquals(methodNames.size(), 2);
        assert methodNames.contains("getProperty");
        assert methodNames.contains("isBoolProperty");
    }

    @Test
    public void isSetter() throws Exception {
        Set<String> methodNames = new HashSet<>();
        for (Method method : TestBean.class.getDeclaredMethods()) {
            if (BeanUtils.isSetter(method)) {
                methodNames.add(method.getName());
            }
        }

        assertEquals(methodNames.size(), 2);
        assert methodNames.contains("setProperty");
        assert methodNames.contains("setProperty1");
    }

    @Test
    public void extractPropertyNameValid() throws Exception {
        assertEquals(BeanUtils.extractPropertyName("getProperty"), "property");
        assertEquals(BeanUtils.extractPropertyName("getProperty1"), "property1");
        assertEquals(BeanUtils.extractPropertyName("isProperty"), "property");
        assertEquals(BeanUtils.extractPropertyName("isProperty1"), "property1");
        assertEquals(BeanUtils.extractPropertyName("setProperty"), "property");
        assertEquals(BeanUtils.extractPropertyName("setProperty1"), "property1");
    }

    @Test
    public void extractPropertyNameInvalid() throws Exception {
        assertEquals(BeanUtils.extractPropertyName("abcd"), null);
        assertEquals(BeanUtils.extractPropertyName("get"), null);
        assertEquals(BeanUtils.extractPropertyName("getproperty"), null);
        assertEquals(BeanUtils.extractPropertyName("GetProperty"), null);
        assertEquals(BeanUtils.extractPropertyName("get1Property"), null);
        assertEquals(BeanUtils.extractPropertyName("is"), null);
        assertEquals(BeanUtils.extractPropertyName("isproperty"), null);
        assertEquals(BeanUtils.extractPropertyName("IsProperty"), null);
        assertEquals(BeanUtils.extractPropertyName("is1Property"), null);
        assertEquals(BeanUtils.extractPropertyName("set"), null);
        assertEquals(BeanUtils.extractPropertyName("setproperty"), null);
        assertEquals(BeanUtils.extractPropertyName("SetProperty"), null);
        assertEquals(BeanUtils.extractPropertyName("set1Property"), null);
    }

    private static class TestBean {
        private String property;
        private boolean boolProperty;

        public String getProperty() {
            return property;
        }

        public String getproperty2() {
            return property;
        }

        public String GetProperty3() {
            return property;
        }

        public String get4Property4() {
            return property;
        }

        public void getProperty5() {
        }

        public String getProperty6(String param) {
            return property;
        }

        public boolean isBoolProperty() {
            return boolProperty;
        }

        public boolean isboolProperty3() {
            return boolProperty;
        }

        public boolean IsBoolProperty3() {
            return boolProperty;
        }

        public boolean is4BoolProperty4() {
            return boolProperty;
        }

        public void isBoolProperty5() {
        }

        public boolean isBoolProperty6(String param) {
            return boolProperty;
        }

        public void setProperty(String property) {
            this.property = property;
        }

        public void setProperty1(boolean boolProperty) {
            this.boolProperty = boolProperty;
        }

        public void setproperty2(String property) {
            this.property = property;
        }

        public void SetProperty3(String property) {
            this.property = property;
        }

        public void set4Property4(String property) {
            this.property = property;
        }

        public void setProperty5() {
        }

        public String setProperty6(String property) {
            this.property = property;
            return property;
        }

        public void setProperty7(String property, String param) {
            this.property = property;
        }
    }

    private static class TestBean2 {
        public TestBean2() {
        }
    }

    private static class TestBean3 {
        public TestBean3(String param) {
        }
    }
}
