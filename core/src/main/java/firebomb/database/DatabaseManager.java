package firebomb.database;

import java8.util.concurrent.CompletableFuture;

import java.util.Map;

/**
 * Database connection interface
 */
public interface DatabaseManager {
    CompletableFuture<Data> read(String path);

    String generateId(String path);

    CompletableFuture<Void> write(String path, Map<String, Object> writeMap);
}
