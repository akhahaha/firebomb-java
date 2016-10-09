package firebomb.database;

import com.google.firebase.database.DataSnapshot;

public class FirebaseData extends Data {
    private DataSnapshot snapshot;

    public FirebaseData(DataSnapshot snapshot) {
        super(snapshot.getKey());
        this.snapshot = snapshot;

        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
            putChild(new FirebaseData(dataSnapshot));
        }

        if (isLeaf()) {
            setValue(snapshot.getValue());
        }
    }

    @Override
    public <T> T getValue(Class<T> valueType) {
        return snapshot.getValue(valueType);
    }
}
