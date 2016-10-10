package firebomb.criteria;

public abstract class PropertyCriterion {
    private String propertyName;

    public PropertyCriterion(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }
}
