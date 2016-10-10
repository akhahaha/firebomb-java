package firebomb.criteria;

public class Restrictions {
    public static EqualsCriterion eq(String propertyName, Boolean value) {
        return new EqualsCriterion(propertyName, value);
    }

    public static EqualsCriterion eq(String propertyName, Double value) {
        return new EqualsCriterion(propertyName, value);
    }

    public static EqualsCriterion eq(String propertyName, String value) {
        return new EqualsCriterion(propertyName, value);
    }

    public static RangeCriterion startAt(String propertyName, Boolean startValue) {
        return new RangeCriterion(propertyName, startValue, null);
    }

    public static RangeCriterion endAt(String propertyName, Boolean endValue) {
        return new RangeCriterion(propertyName, null, endValue);
    }

    public static RangeCriterion between(String propertyName, Boolean startValue, Boolean endValue) {
        return new RangeCriterion(propertyName, startValue, endValue);
    }

    public static RangeCriterion startAt(String propertyName, Double startValue) {
        return new RangeCriterion(propertyName, startValue, null);
    }

    public static RangeCriterion endAt(String propertyName, Double endValue) {
        return new RangeCriterion(propertyName, null, endValue);
    }

    public static RangeCriterion between(String propertyName, Double startValue, Double endValue) {
        return new RangeCriterion(propertyName, startValue, endValue);
    }

    public static RangeCriterion startAt(String propertyName, String startValue) {
        return new RangeCriterion(propertyName, startValue, null);
    }

    public static RangeCriterion endAt(String propertyName, String endValue) {
        return new RangeCriterion(propertyName, null, endValue);
    }

    public static RangeCriterion between(String propertyName, String startValue, String endValue) {
        return new RangeCriterion(propertyName, startValue, endValue);
    }

    public static LimitCriterion limitToFirst(int limit) {
        return new LimitCriterion(true, limit);
    }

    public static LimitCriterion limitToLast(int limit) {
        return new LimitCriterion(false, limit);
    }
}
