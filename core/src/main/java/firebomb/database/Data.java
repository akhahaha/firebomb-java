package firebomb.database;

import firebomb.util.StringUtils;

import java.util.*;

public class Data {
    private static final String PATH_SPLITTER = "/";

    private String key;
    private Object value;
    private Map<String, Data> children = new HashMap<>();

    public Data() {
    }

    public Data(String key) {
        this.key = key;
    }

    public Data(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public <T> T getValue(Class<T> valueType) {
        return (T) value;
    }

    public void setValue(Object value) {
        if (!children.isEmpty()) {
            throw new IllegalStateException("Value cannot be set on Data node with children.");
        }

        this.value = value;
    }

    public void setValue(String path, Object value) {
        Data currNode = this;
        for (String nodeName : path.split(PATH_SPLITTER)) {
            if (currNode.child(nodeName) == null) {
                currNode.putChild(new Data(nodeName));
            }
            currNode = currNode.child(nodeName);
        }

        currNode.setValue(value);
    }

    public Data child(String path) {
        String[] nodeNames = path.split(PATH_SPLITTER);
        Data currNode = children.get(nodeNames[0]);
        for (int i = 1; i < nodeNames.length; i++) {
            currNode = currNode.child(nodeNames[i]);
            if (currNode == null) {
                break;
            }
        }

        return currNode;
    }

    protected void putChild(Data child) {
        if (value != null) {
            throw new IllegalStateException("Child cannot be added to Data node with value.");
        }

        children.put(child.getKey(), child);
    }

    public List<Data> getChildren() {
        return new ArrayList<>(children.values());
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public Map<String, Object> toChildMap() {
        return toChildMap(false);
    }

    public Map<String, Object> toChildDeleteMap() {
        return toChildMap(true);
    }

    public Map<String, Object> toChildMap(boolean isDelete) {
        Map<String, Object> map = new HashMap<>();

        Stack<Data> nodeStack = new Stack<>();
        Stack<String> pathStack = new Stack<>();
        nodeStack.push(this);

        while(!nodeStack.isEmpty()) {
            Data currNode = nodeStack.pop();
            String currPath = pathStack.isEmpty() ? "" : pathStack.pop();

            if (currNode.isLeaf()) {
                map.put(currPath, isDelete ? null : currNode.getValue());
            } else {
                for (Data child : currNode.getChildren()) {
                    nodeStack.push(child);
                    pathStack.push(currPath.isEmpty() ? child.getKey() : StringUtils.path(currPath, child.getKey()));
                }
            }
        }

        return map;
    }

    public void setChildMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            setValue(entry.getKey(), entry.getValue());
        }
    }
}
