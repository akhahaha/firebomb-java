package firebomb.database;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebase Java server {@link DataSnapshot} wrapper
 */
public class FirebaseServerData implements Data {
    private DataSnapshot snapshot;

    public FirebaseServerData(DataSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public String getKey() {
        return snapshot.getKey();
    }

    @Override
    public Object getValue() {
        return snapshot.getValue();
    }

    @Override
    public <T> T getValue(Class<T> valueType) {
        return snapshot.getValue(valueType);
    }

    @Override
    public Data child(String path) {
        return new FirebaseServerData(snapshot.child(path));
    }

    @Override
    public List<Data> getChildren() {
        List<Data> children = new ArrayList<>();
        snapshot.getChildren().forEach(dataSnapshot -> children.add(new FirebaseServerData(dataSnapshot)));
        return children;
    }
}
