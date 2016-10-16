package firebomb;

import firebomb.criteria.Criteria;
import firebomb.database.Data;
import firebomb.database.DatabaseManager;
import firebomb.definition.DefinitionException;
import firebomb.definition.EntityDefinition;
import firebomb.definition.EntityDefinitionManager;
import firebomb.util.StringUtils;
import java8.util.concurrent.CompletableFuture;
import java8.util.concurrent.CompletionStage;
import java8.util.function.Consumer;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Firebomb {
    private static Firebomb ourInstance;

    public static Firebomb getInstance() {
        if (ourInstance == null) {
            throw new IllegalStateException("Firebomb instance not initialized.");
        }

        return ourInstance;
    }

    private EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();
    private EntityParser entityParser = new DefaultEntityParser();
    private DatabaseManager connection;
    private String rootPath = ""; // Default Firebase root

    public static void initialize(DatabaseManager connection) {
        ourInstance = new Firebomb(connection);
    }

    public static void initialize(DatabaseManager connection, String rootPath) {
        initialize(connection);
        ourInstance.setRootPath(rootPath);
    }

    public Firebomb(DatabaseManager connection) {
        this.connection = connection;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public void setEntityParser(EntityParser entityParser) {
        this.entityParser = entityParser;
    }

    public <T> CompletableFuture<T> find(final Class<T> entityType, String id) {
        final CompletableFuture<T> promise = new CompletableFuture<>();

        final EntityDefinition entityDef;
        try {
            entityDef = entityDefinitionManager.getDefinition(entityType);
        } catch (Exception e) {
            promise.completeExceptionally(e);
            return promise;
        }

        String path = StringUtils.path(entityDef.getReference(), id);
        connection.read(StringUtils.path(rootPath, path))
                .thenAccept(new Consumer<Data>() {
                    @Override
                    public void accept(Data entityData) {
                        promise.complete(entityParser.deserialize(entityType, entityData));
                    }
                })
                .exceptionally(new Function<Throwable, Void>() {
                    @Override
                    public Void apply(Throwable throwable) {
                        promise.completeExceptionally(throwable);
                        return null;
                    }
                });

        return promise;
    }

    public <T> CompletableFuture<List<T>> query(final Criteria<T> criteria) {
        final CompletableFuture<List<T>> promise = new CompletableFuture<>();

        final Class<T> entityType = criteria.getEntityType();
        EntityDefinition entityDef;
        try {
            entityDef = entityDefinitionManager.getDefinition(entityType);
        } catch (DefinitionException e) {
            promise.completeExceptionally(e);
            return promise;
        }

        connection.query(StringUtils.path(rootPath, entityDef.getReference()), criteria)
                .thenAccept(new Consumer<Data>() {
                    @Override
                    public void accept(Data data) {
                        List<T> results = new ArrayList<T>();
                        for (Data entityData : data.getChildren()) {
                            T entity = entityParser.deserialize(entityType, entityData);
                            // Filter
                            if (criteria.match(entity)) {
                                results.add(entity);
                            }
                        }
                        promise.complete(results);
                    }
                })
                .exceptionally(new Function<Throwable, Void>() {
                    @Override
                    public Void apply(Throwable throwable) {
                        promise.completeExceptionally(throwable);
                        return null;
                    }
                });

        return promise;
    }

    public <T> CompletableFuture<T> persist(final T entity) {
        CompletableFuture<T> promise = new CompletableFuture<>();

        // Construct entity definition
        Class<T> entityType = (Class<T>) entity.getClass();
        EntityDefinition entityDef;
        try {
            entityDef = entityDefinitionManager.getDefinition(entityType);
        } catch (DefinitionException e) {
            promise.completeExceptionally(e);
            return promise;
        }

        // Get Id if necessary
        String idName = entityDef.getIdName();
        String entityId = entityDef.getId(entity);
        if (entityId == null && !entityDef.getIdDefinition().isGeneratedValue()) {
            promise.completeExceptionally(new FirebombException(
                    "Id '" + entityDef.getName() + "." + idName + "' cannot be null."));
            return promise;
        } else if (entityId == null) {
            entityId = connection.generateId(entityDef.getReference());
            entityDef.setId(entity, entityId);
            return connection.write(rootPath, entityParser.serialize(entity)).thenApply(new Function<Void, T>() {
                @Override
                public T apply(Void aVoid) {
                    return entity;
                }
            });
        } else {
            return find(entityType, entityId)
                    .thenCompose(new Function<T, CompletionStage<Void>>() {
                        @Override
                        public CompletionStage<Void> apply(T existingEntity) {
                            Data writeData = new Data();
                            if (existingEntity != null) {
                                writeData.setChildMap(entityParser.serialize(existingEntity).toChildDeleteMap());
                            }
                            writeData.setChildMap(entityParser.serialize(entity).toChildMap());
                            return connection.write(rootPath, writeData);
                        }
                    })
                    .thenApply(new Function<Void, T>() {
                        @Override
                        public T apply(Void aVoid) {
                            return entity;
                        }
                    });
        }
    }

    public <T> CompletableFuture<Void> remove(Class<T> entityType, String id) {
        // Cleanup currently persisted foreign indexes
        // TODO: Delete ManyToOne foreign entities?
        return find(entityType, id).thenCompose(new Function<T, CompletionStage<Void>>() {
            @Override
            public CompletionStage<Void> apply(T entity) {
                Data deleteData = new Data();
                deleteData.setChildMap(entityParser.serialize(entity).toChildDeleteMap());
                return connection.write(rootPath, deleteData);
            }
        });
    }
}
