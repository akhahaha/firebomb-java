package firebomb.database;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class DataTest {
    @Test
    public void toChildMap() throws Exception {
        Data data = new Data("root");
        data.setValue("a/b/c", "val1");
        data.setValue("a/b/d", "val2");
        data.setValue("a/a/d", "val3");
        Map<String, Object> map = data.toChildMap();

        assertEquals(3, map.size());
        assertEquals("val1", map.get("a/b/c"));
        assertEquals("val2", map.get("a/b/d"));
        assertEquals("val3", map.get("a/a/d"));
    }

    @Test
    public void toChildMapRootOnly() throws Exception {
        Data data = new Data("root");
        data.setValue("val1");
        Map<String, Object> map = data.toChildMap();

        assertEquals(map.get(""), "val1");
    }

    @Test
    public void toChildMapDelete() throws Exception {
        Data data = new Data("root");
        data.setValue("a/b/c", "val1");
        data.setValue("a/b/d", "val2");
        data.setValue("a/a/d", "val3");
        Map<String, Object> map = data.toChildMap();

        assertEquals(3, map.size());
        assertEquals("val1", map.get("a/b/c"));
        assertEquals("val2", map.get("a/b/d"));
        assertEquals("val3", map.get("a/a/d"));
    }

    @Test
    public void setChildMap() throws Exception {
        Data origData = new Data("root");
        origData.setValue("a/b/c", "val1");
        origData.setValue("a/b/d", "val2");
        origData.setValue("a/a/d", "val3");
        Data data = new Data();
        data.setChildMap(origData.toChildMap());
        Map<String, Object> map = data.toChildMap();

        assertEquals(3, map.size());
        assertEquals("val1", map.get("a/b/c"));
        assertEquals("val2", map.get("a/b/d"));
        assertEquals("val3", map.get("a/a/d"));
    }
}
