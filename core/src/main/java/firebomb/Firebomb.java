package firebomb;

import firebomb.database.Data;
import firebomb.database.DatabaseManager;
import firebomb.definition.*;
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

    public <T> CompletableFuture<T> find(final Class<T> entityType, String id) {
        final CompletableFuture<T> promise = new CompletableFuture<>();

        final EntityDefinition entityDef;
        try {
            entityDef = EntityDefinitionManager.getInstance().getDefinition(entityType);
        } catch (Exception e) {
            promise.completeExceptionally(e);
            return promise;
        }

        String path = path(entityDef.getReference(), id);
        connection.read(path(rootPath, path)).thenAccept(
                new Consumer<Data>() {
                    @Override
                    public void accept(Data entityData) {
                        try {
                            if (entityData.getValue() == null) {
                                promise.complete(null);
                                return;
                            }

                            T entity = entityType.newInstance();

                            // Set ID
                            entityDef.setId(entity, (String) entityData.child(entityDef.getIdName()).getValue());

                            // Set fields
                            for (FieldDefinition fieldDef : entityDef.getFieldDefinitions()) {
                                fieldDef.set(entity, entityData.child(fieldDef.getName()).getValue(fieldDef.getType()));
                                // TODO Verify lists
                            }

                            // Set relations
                            // TODO Implement eager loading
                            for (ManyToManyDefinition manyToManyDef : entityDef.getManyToManyDefinitions()) {
                                List<Object> foreignEntities = new ArrayList<>();
                                for (Data foreignEntityData : entityData.child(manyToManyDef.getName()).getChildren()) {
                                    foreignEntities.add(foreignEntityData.getValue(manyToManyDef.getForeignEntityType()));
                                }
                                manyToManyDef.set(entity, foreignEntities);
                            }

                            for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
                                manyToOneDef.set(entity, entityData.child(manyToOneDef.getName()).getChildren().get(0)
                                        .getValue(manyToOneDef.getForeignEntityType()));
                            }

                            for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
                                List<Object> foreignEntities = new ArrayList<>();
                                for (Data foreignEntityData : entityData.child(oneToManyDef.getName()).getChildren()) {
                                    foreignEntityData.getValue(oneToManyDef.getForeignEntityType());
                                }
                                oneToManyDef.set(entity, foreignEntities);
                            }

                            promise.complete(entity);
                        } catch (InstantiationException | IllegalAccessException e) {
                            promise.completeExceptionally(e);
                        }
                    }
                }).exceptionally(
                new Function<Throwable, Void>() {
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
        Class entityType = entity.getClass();
        EntityDefinition entityDef;
        try {
            entityDef = EntityDefinitionManager.getInstance().getDefinition(entityType);
        } catch (DefinitionException e) {
            promise.completeExceptionally(e);
            return promise;
        }

        final Map<String, Object> writeMap = new HashMap<>();

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
            writeMap.putAll(constructWriteMap(entity));
            return connection.write(rootPath, writeMap).thenApply(new Function<Void, T>() {
                @Override
                public T apply(Void aVoid) {
                    return entity;
                }
            });
        } else {
            return constructDeleteMap(entityType, entityId)
                    .thenCompose(
                            new Function<Map<String, Object>, CompletionStage<Void>>() {
                                @Override
                                public CompletionStage<Void> apply(Map<String, Object> stringObjectMap) {
                                    writeMap.putAll(stringObjectMap);
                                    writeMap.putAll(constructWriteMap(entity));
                                    return connection.write(rootPath, writeMap);
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

    public CompletableFuture<Void> remove(Class entityType, String id) {
        CompletableFuture<Void> promise = new CompletableFuture<>();

        // Construct entity definition
        EntityDefinition entityDef;
        try {
            entityDef = EntityDefinitionManager.getInstance().getDefinition(entityType);
        } catch (DefinitionException e) {
            promise.completeExceptionally(e);
            return promise;
        }

        final Map<String, Object> writeMap = new HashMap<>();

        // Cleanup currently persisted foreign indexes
        // TODO: Delete ManyToOne foreign entities?
        return constructDeleteMap(entityType, id).thenCompose(new Function<Map<String, Object>, CompletionStage<Void>>() {
            @Override
            public CompletionStage<Void> apply(Map<String, Object> stringObjectMap) {
                return connection.write(rootPath, stringObjectMap);
            }
        }).exceptionally(new Function<Throwable, Void>() {
            @Override
            public Void apply(Throwable throwable) {
                throwable.printStackTrace();
                return null;
            }
        });
    }

    private Map<String, Object> constructWriteMap(Object entity) {
        Map<String, Object> writeMap = new HashMap<>();

        Class entityType = entity.getClass();
        EntityDefinition entityDef = EntityDefinitionManager.getInstance().getDefinition(entityType);

        // Add Id
        String idName = entityDef.getIdName();
        String entityId = entityDef.getId(entity);
        String entityPath = path(entityDef.getReference(), entityId);
        writeMap.put(path(entityPath, entityDef.getIdName()), entityId);

        // Add fields
        for (FieldDefinition fieldDef : entityDef.getFieldDefinitions()) {
            Object fieldValue = fieldDef.get(entity);
            if (fieldDef.isNonNull() && fieldValue == null) {
                throw new FirebombException("Non-null field '" + entityDef.getName() + "." + fieldDef.getName() +
                        "' cannot be null.");
            }
            writeMap.put(path(entityPath, fieldDef.getName()), fieldValue);
        }

        // Add ManyToMany
        for (ManyToManyDefinition manyToManyDef : entityDef.getManyToManyDefinitions()) {
            String foreignIdName = manyToManyDef.getForeignIdName();
            for (Object foreignEntity : manyToManyDef.get(entity)) {
                String foreignId = manyToManyDef.getForeignId(foreignEntity);
                writeMap.put(path(entityPath, manyToManyDef.getName(), foreignId), kvp(foreignIdName, foreignId));
                writeMap.put(path(manyToManyDef.constructForeignIndexPath(foreignId), entityId), kvp(idName, entityId));
            }
        }

        // Add ManyToOne
        for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
            Object foreignEntity = manyToOneDef.get(entity);
            String foreignIdName = manyToOneDef.getForeignIdName();
            String foreignId = manyToOneDef.getForeignId(foreignEntity);
            writeMap.put(path(entityPath, manyToOneDef.getName(), foreignId), kvp(foreignIdName, foreignId));
            writeMap.put(path(manyToOneDef.constructForeignIndexPath(foreignId), entityId), kvp(idName, entityId));
        }

        // Add OneToMany
        for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
            String foreignIdName = oneToManyDef.getForeignIdName();
            for (Object foreignEntity : oneToManyDef.get(entity)) {
                String foreignId = oneToManyDef.getForeignId(foreignEntity);
                writeMap.put(path(entityPath, oneToManyDef.getName(), foreignId), kvp(foreignIdName, foreignId));
                writeMap.put(oneToManyDef.constructForeignFieldPath(foreignId), entityId);
            }
        }

        return writeMap;
    }

    private CompletableFuture<Map<String, Object>> constructDeleteMap(Class entityType, final String id) {
        final EntityDefinition entityDefinition = EntityDefinitionManager.getInstance().getDefinition(entityType);
        final CompletableFuture<Map<String, Object>> promise = new CompletableFuture<>();
        final Map<String, Object> writeMap = new HashMap<>();

        find(entityType, id).thenAccept(new Consumer() {
            @Override
            public void accept(Object entity) {
                if (entity == null) {
                    promise.complete(writeMap);
                    return;
                }

                String entityPath = path(entityDefinition.getReference(), id);

                // Add Id
                writeMap.put(path(entityPath, entityDefinition.getIdName()), null);

                // Add fields
                for (FieldDefinition fieldDef : entityDefinition.getFieldDefinitions()) {
                    writeMap.put(path(entityPath, fieldDef.getName()), null);
                }

                // Add ManyToMany
                for (ManyToManyDefinition manyToManyDef : entityDefinition.getManyToManyDefinitions()) {
                    for (Object foreignEntity : manyToManyDef.get(entity)) {
                        String foreignId = manyToManyDef.getForeignId(foreignEntity);
                        writeMap.put(path(entityPath, manyToManyDef.getName(), foreignId), null);
                        writeMap.put(path(manyToManyDef.constructForeignIndexPath(foreignId), id), null);
                    }
                }

                // Add ManyToOne
                for (ManyToOneDefinition manyToOneDef : entityDefinition.getManyToOneDefinitions()) {
                    Object foreignEntity = manyToOneDef.get(entity);
                    String foreignId = manyToOneDef.getForeignId(foreignEntity);
                    writeMap.put(path(entityPath, manyToOneDef.getName(), foreignId), null);
                    writeMap.put(path(manyToOneDef.constructForeignIndexPath(foreignId), id), null);
                }

                // Add OneToMany
                for (OneToManyDefinition oneToManyDef : entityDefinition.getOneToManyDefinitions()) {
                    for (Object foreignEntity : oneToManyDef.get(entity)) {
                        String foreignId = oneToManyDef.getForeignId(foreignEntity);
                        writeMap.put(path(entityPath, oneToManyDef.getName(), foreignId), null);
                        writeMap.put(oneToManyDef.constructForeignFieldPath(foreignId), null);
                    }
                }

                promise.complete(writeMap);
            }
        });

        return promise;
    }

    private static String path(String... nodes) {
        return StringUtils.join("/", nodes);
    }

    private static Map<String, String> kvp(String key, String value) {
        Map<String, String> kvp = new HashMap<>();
        kvp.put(key, value);
        return kvp;
    }
}
