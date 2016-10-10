package firebomb.criteria;

public class RangeCriterion extends PropertyCriterion {
    public enum ValueType {
        BOOLEAN, DOUBLE, STRING
    }

    private ValueType valueType;
    private Object startValue;
    private Object endValue;

    public RangeCriterion(String propertyName, Boolean startValue, Boolean endValue) {
        super(propertyName);
        this.valueType = ValueType.BOOLEAN;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public RangeCriterion(String propertyName, Double startValue, Double endValue) {
        super(propertyName);
        this.valueType = ValueType.DOUBLE;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public RangeCriterion(String propertyName, String startValue, String endValue) {
        super(propertyName);
        this.valueType = ValueType.STRING;
        this.startValue = startValue;
        this.endValue = endValue;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Object getStartValue() {
        return startValue;
    }

    public Object getEndValue() {
        return endValue;
    }
}
