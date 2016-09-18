package firebomb.database;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Database connection interface
 */
public interface DatabaseManager {
    CompletableFuture<Data> read(String path);

    String generateId(String path);

    CompletableFuture<Void> write(String path, Map<String, Object> writeMap);
}
