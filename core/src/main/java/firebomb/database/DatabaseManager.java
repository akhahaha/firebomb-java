package firebomb.database;

import firebomb.criteria.Criteria;
import java8.util.concurrent.CompletableFuture;

import java.util.Map;

/**
 * Database connection interface
 */
public interface DatabaseManager {
    CompletableFuture<Data> read(String path);

    CompletableFuture<Data> query(String path, Criteria<?> criteria);

    String generateId(String path);

    CompletableFuture<Void> write(String path, Data data);
}
