package firebomb.criteria;

import firebomb.definition.EntityDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Criteria<T> {
    private Class<T> entityType;
    private EntityDefinition entityDef;
    private Map<String, EqualsCriterion> equalsCriteria = new HashMap<>();
    private Map<String, RangeCriterion> rangeCriteria = new HashMap<>();
    private LimitCriterion limitCriterion;

    public Criteria(Class<T> entityType) {
        this.entityType = entityType;
        entityDef = new EntityDefinition(entityType);
    }

    public Class<T> getEntityType() {
        return entityType;
    }

    public List<EqualsCriterion> getEqualsCriteria() {
        return new ArrayList<>(equalsCriteria.values());
    }

    public Criteria add(EqualsCriterion equalsCriterion) {
        String propertyName = equalsCriterion.getPropertyName();
        if (containsPropertyCriterion(propertyName)) {
            throw new IllegalStateException("Property criterion for '" + propertyName + "' already exists.");
        }
        equalsCriteria.put(equalsCriterion.getPropertyName(), equalsCriterion);
        return this;
    }

    public List<RangeCriterion> getRangeCriteria() {
        return new ArrayList<>(rangeCriteria.values());
    }

    public Criteria add(RangeCriterion rangeCriterion) {
        String propertyName = rangeCriterion.getPropertyName();
        if (containsPropertyCriterion(propertyName)) {
            throw new IllegalStateException("Property criterion for '" + propertyName + "' already exists.");
        }
        rangeCriteria.put(rangeCriterion.getPropertyName(), rangeCriterion);
        return this;
    }

    public LimitCriterion getLimitCriterion() {
        return limitCriterion;
    }

    public Criteria add(LimitCriterion limitCriterion) {
        if (this.limitCriterion != null) {
            throw new IllegalStateException("Limit criterion already exists.");
        }

        this.limitCriterion = limitCriterion;
        return this;
    }

    public boolean containsPropertyCriterion(String propertyName) {
        return equalsCriteria.containsKey(propertyName) || rangeCriteria.containsKey(propertyName);
    }

    public boolean match(T entity) {
        for (EqualsCriterion equalsCriterion : getEqualsCriteria()) {
            String propertyName = equalsCriterion.getPropertyName();
            if (!entityDef.hasProperty(propertyName)) {
                return false;
            }

            Object value = entityDef.getProperty(propertyName).get(entity);
            if (!equalsCriterion.getValue().equals(value)) {
                return false;
            }
        }

        for (RangeCriterion rangeCriterion : getRangeCriteria()) {
            String propertyName = rangeCriterion.getPropertyName();
            if (!entityDef.hasProperty(propertyName)) {
                return false;
            }

            Object value = entityDef.getProperty(propertyName).get(entity);
            if (rangeCriterion.getStartValue() != null) {
                switch (rangeCriterion.getValueType()) {
                    case BOOLEAN:
                        // TODO: Verify correctness
                        if ((Boolean) rangeCriterion.getStartValue() && !((Boolean) value)) {
                            return false;
                        }
                        break;
                    case STRING:
                        // If returns > 0 then the parameter passed to the compareTo method is lexicographically first.
                        if (((String) rangeCriterion.getStartValue()).compareTo((String) value) > 0) {
                            return false;
                        }
                        break;
                    case DOUBLE:
                        if (((Double) rangeCriterion.getStartValue()) > ((Double) value)) {
                            return false;
                        }
                        break;
                }
            }

            if (rangeCriterion.getEndValue() != null) {
                switch (rangeCriterion.getValueType()) {
                    case BOOLEAN:
                        // TODO: Verify correctness
                        if (!(Boolean) rangeCriterion.getEndValue() && (Boolean) value) {
                            return false;
                        }
                        break;
                    case STRING:
                        // If returns < 0 then the String calling the method is lexicographically first
                        if (((String) rangeCriterion.getEndValue()).compareTo((String) value) < 0) {
                            return false;
                        }
                        break;
                    case DOUBLE:
                        if (((Double) rangeCriterion.getEndValue()) < ((Double) value)) {
                            return false;
                        }
                        break;
                }
            }
        }

        return true;
    }
}
