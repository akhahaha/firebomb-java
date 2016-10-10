package firebomb.criteria;

public class LimitCriterion {
    private boolean isLimitToFirst;
    private int limit;

    public LimitCriterion(boolean isLimitToFirst, int limit) {
        this.isLimitToFirst = isLimitToFirst;
        this.limit = limit;
    }

    public boolean isLimitToFirst() {
        return isLimitToFirst;
    }

    public int getLimit() {
        return limit;
    }
}
