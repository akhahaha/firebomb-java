package firebomb.database;

import com.google.firebase.database.*;
import firebomb.criteria.Criteria;
import firebomb.criteria.EqualsCriterion;
import firebomb.criteria.LimitCriterion;
import firebomb.criteria.RangeCriterion;
import java8.util.concurrent.CompletableFuture;

/**
 * Firebase Java server {@link FirebaseDatabase} connection wrapper
 */
public class FirebaseManager implements DatabaseManager {
    private FirebaseDatabase database;

    public FirebaseManager(FirebaseDatabase database) {
        this.database = database;
    }

    public CompletableFuture<Data> read(String path) {
        final CompletableFuture<Data> promise = new CompletableFuture<>();

        database.getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promise.complete(new FirebaseData(dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                promise.completeExceptionally(databaseError.toException());
            }
        });

        return promise;
    }

    public CompletableFuture<Data> query(String path, Criteria<?> criteria) {
        final CompletableFuture<Data> promise = new CompletableFuture<>();

        // Add criteria
        // Can only add a single order-by filter
        Query query = database.getReference(path);
        if (!criteria.getRangeCriteria().isEmpty()) {
            RangeCriterion rangeCriterion = criteria.getRangeCriteria().get(0);
            query = query.orderByChild(rangeCriterion.getPropertyName());
            switch (rangeCriterion.getValueType()) {
                case BOOLEAN:
                    if (rangeCriterion.getStartValue() != null) {
                        query = query.startAt((Boolean) rangeCriterion.getStartValue());
                    }
                    if (rangeCriterion.getEndValue() != null) {
                        query = query.endAt((Boolean) rangeCriterion.getEndValue());
                    }
                    break;
                case DOUBLE:
                    if (rangeCriterion.getStartValue() != null) {
                        query = query.startAt((Double) rangeCriterion.getStartValue());
                    }
                    if (rangeCriterion.getEndValue() != null) {
                        query = query.endAt((Double) rangeCriterion.getEndValue());
                    }
                    break;
                case STRING:
                    if (rangeCriterion.getStartValue() != null) {
                        query = query.startAt((String) rangeCriterion.getStartValue());
                    }
                    if (rangeCriterion.getEndValue() != null) {
                        query = query.endAt((String) rangeCriterion.getEndValue());
                    }
                    break;
            }
        } else if (!criteria.getEqualsCriteria().isEmpty()) {
            EqualsCriterion equalsCriterion = criteria.getEqualsCriteria().get(0);
            query = query.orderByChild(equalsCriterion.getPropertyName());
            switch (equalsCriterion.getValueType()) {
                case BOOLEAN:
                    query = query.equalTo((Boolean) equalsCriterion.getValue());
                    break;
                case DOUBLE:
                    query = query.equalTo((Double) equalsCriterion.getValue());
                    break;
                case STRING:
                    query = query.equalTo((String) equalsCriterion.getValue());
                    break;
            }
        }

        LimitCriterion limitCriterion = criteria.getLimitCriterion();
        if (limitCriterion != null) {
            if (limitCriterion.isLimitToFirst()) {
                query = query.limitToFirst(limitCriterion.getLimit());
            } else {
                query = query.limitToLast(limitCriterion.getLimit());
            }
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promise.complete(new FirebaseData(dataSnapshot));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                promise.completeExceptionally(databaseError.toException());
            }
        });

        return promise;
    }

    public String generateId(String path) {
        return database.getReference(path).push().getKey();
    }

    public CompletableFuture<Void> write(String path, Data data) {
        final CompletableFuture<Void> promise = new CompletableFuture<>();
        database.getReference(path).updateChildren(data.toChildMap(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    promise.complete(null);
                } else {
                    promise.completeExceptionally(databaseError.toException());
                }
            }
        });

        return promise;
    }
}
