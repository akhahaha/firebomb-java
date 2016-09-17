package firebomb.database;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Firebase Java server {@link FirebaseDatabase} connection wrapper
 */
public class FirebaseServerManager implements DatabaseManager {
    private FirebaseDatabase database;

    public FirebaseServerManager(FirebaseDatabase database) {
        this.database = database;
    }

    public CompletableFuture<Data> read(String path) {
        CompletableFuture<Data> promise = new CompletableFuture<>();

        database.getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                promise.complete(new FirebaseServerData(dataSnapshot));
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
        CompletableFuture<Void> promise = new CompletableFuture<>();
        database.getReference(path).updateChildren(writeMap, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                promise.complete(null);
            } else {
                promise.completeExceptionally(databaseError.toException());
            }
        });

        return promise;
    }
}
