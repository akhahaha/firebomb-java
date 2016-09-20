package firebomb.database;

import com.google.firebase.database.*;
import java8.util.concurrent.CompletableFuture;

import java.util.Map;

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

    public String generateId(String path) {
        return database.getReference(path).push().getKey();
    }

    public CompletableFuture<Void> write(String path, Map<String, Object> writeMap) {
        final CompletableFuture<Void> promise = new CompletableFuture<>();
        database.getReference(path).updateChildren(writeMap, new DatabaseReference.CompletionListener() {
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
