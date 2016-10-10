package firebomb.criteria;

public class EqualsCriterion extends PropertyCriterion {
    public enum ValueType {
        BOOLEAN, DOUBLE, STRING
    }

    private ValueType valueType;
    private Object value;

    public EqualsCriterion(String propertyName, Boolean value) {
        super(propertyName);
        this.valueType = ValueType.BOOLEAN;
        this.value = value;
    }

    public EqualsCriterion(String propertyName, Double value) {
        super(propertyName);
        this.valueType = ValueType.DOUBLE;
        this.value = value;
    }

    public EqualsCriterion(String propertyName, String value) {
        super(propertyName);
        this.valueType = ValueType.STRING;
        this.value = value;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Object getValue() {
        return value;
    }
}
