package firebomb.beanutils;

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
        assertEquals(1, properties.size());
        assertEquals("property", properties.get(0).getName());
    }

    @Test
    public void isGetter() throws Exception {
        Set<String> methodNames = new HashSet<>();
        for (Method method : TestBean.class.getDeclaredMethods()) {
            if (BeanUtils.isGetter(method)) {
                methodNames.add(method.getName());
            }
        }

        assertEquals(2, methodNames.size());
        assertTrue(methodNames.contains("getProperty"));
        assertTrue(methodNames.contains("isBoolProperty"));
    }

    @Test
    public void isSetter() throws Exception {
        Set<String> methodNames = new HashSet<>();
        for (Method method : TestBean.class.getDeclaredMethods()) {
            if (BeanUtils.isSetter(method)) {
                methodNames.add(method.getName());
            }
        }

        assertEquals(2, methodNames.size());
        assertTrue(methodNames.contains("setProperty"));
        assertTrue(methodNames.contains("setProperty1"));
    }

    @Test
    public void extractPropertyNameValid() throws Exception {
        assertEquals("property", BeanUtils.extractPropertyName("getProperty"));
        assertEquals("property1", BeanUtils.extractPropertyName("getProperty1"));
        assertEquals("property", BeanUtils.extractPropertyName("isProperty"));
        assertEquals("property1", BeanUtils.extractPropertyName("isProperty1"));
        assertEquals("property", BeanUtils.extractPropertyName("setProperty"));
        assertEquals("property1", BeanUtils.extractPropertyName("setProperty1"));
    }

    @Test
    public void extractPropertyNameInvalid() throws Exception {
        assertNull(BeanUtils.extractPropertyName("abcd"));
        assertNull(BeanUtils.extractPropertyName("get"));
        assertNull(BeanUtils.extractPropertyName("getproperty"));
        assertNull(BeanUtils.extractPropertyName("GetProperty"));
        assertNull(BeanUtils.extractPropertyName("get1Property"));
        assertNull(BeanUtils.extractPropertyName("is"));
        assertNull(BeanUtils.extractPropertyName("isproperty"));
        assertNull(BeanUtils.extractPropertyName("IsProperty"));
        assertNull(BeanUtils.extractPropertyName("is1Property"));
        assertNull(BeanUtils.extractPropertyName("set"));
        assertNull(BeanUtils.extractPropertyName("setproperty"));
        assertNull(BeanUtils.extractPropertyName("SetProperty"));
        assertNull(BeanUtils.extractPropertyName("set1Property"));
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
