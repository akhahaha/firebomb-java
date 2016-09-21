package firebomb;

import firebomb.database.Data;
import firebomb.database.DatabaseManager;
import firebomb.definition.*;
import firebomb.util.StringUtils;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Consumer;
import java8.util.function.Function;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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

    public <T> void find(final Class<T> entityType, String id, final FirebombCallback<T> callback) {
        final EntityDefinition entityDef;
        try {
            entityDef = new EntityDefinition(entityType);
        } catch (Exception e) {
            if (callback != null) {
                callback.onException(e);
            }
            return;
        }

        String path = path(entityDef.getReference(), id);
        connection.read(path(rootPath, path)).thenAccept(
                new Consumer<Data>() {
                    @Override
                    public void accept(Data entityData) {
                        try {
                            if (entityData.getValue() == null) {
                                if (callback != null) {
                                    callback.onComplete(null);
                                }
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

                            if (callback != null) {
                                callback.onComplete(entity);
                            }
                        } catch (InstantiationException | IllegalAccessException e) {
                            if (callback != null) {
                                callback.onException(e);
                            }
                        }
                    }
                }).exceptionally(
                new Function<Throwable, Void>() {
                    @Override
                    public Void apply(Throwable throwable) {
                        if (callback != null) {
                            callback.onException(new Exception(throwable));
                        }
                        return null;
                    }
                });
    }

    public <T> void persist(final T entity, final FirebombCallback<T> callback) {
        // Construct entity definition
        EntityDefinition entityDef;
        try {
            entityDef = new EntityDefinition(entity.getClass());
        } catch (DefinitionException e) {
            if (callback != null) {
                callback.onException(e);
            }
            return;
        }

        Map<String, Object> writeMap = new HashMap<>();

        // Get Id if necessary
        String idName = entityDef.getIdName();
        String entityId = entityDef.getId(entity);
        if (entityId == null && !entityDef.getIdDefinition().isGeneratedValue()) {
            if (callback != null) {
                callback.onException(new FirebombException(
                        "Id '" + entityDef.getName() + "." + idName + "' cannot be null."));
            }
            return;
        } else if (entityId == null) {
            entityId = connection.generateId(entityDef.getReference());
            entityDef.setId(entity, entityId);
        } else {
            // Cleanup currently persisted foreign indexes
            try {
                writeMap.putAll(constructDeleteMap(entityDef, entityId).get());
            } catch (InterruptedException | ExecutionException e) {
                if (callback != null) {
                    callback.onException(e);
                }
                return;
            }
        }

        // Add Id
        String entityPath = path(entityDef.getReference(), entityId);
        writeMap.put(path(entityPath, entityDef.getIdName()), entityId);

        // Add fields
        for (FieldDefinition fieldDef : entityDef.getFieldDefinitions()) {
            Object fieldValue = fieldDef.get(entity);
            if (fieldDef.isNonNull() && fieldValue == null) {
                if (callback != null) {
                    callback.onException(new FirebombException(
                            "Non-null field '" + entityDef.getName() + "." + fieldDef.getName() + "' cannot be null."));
                }
                return;
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

        // Write
        connection.write(rootPath, writeMap).thenAccept(new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                if (callback != null) {
                    callback.onComplete(entity);
                }
            }
        });
    }

    public void remove(Class entityType, String id, final FirebombCallback<Void> callback) {
        // Construct entity definition
        EntityDefinition entityDef;
        try {
            entityDef = new EntityDefinition(entityType);
        } catch (DefinitionException e) {
            if (callback != null) {
                callback.onException(e);
            }
            return;
        }

        Map<String, Object> writeMap = new HashMap<>();

        // Cleanup currently persisted foreign indexes
        try {
            writeMap.putAll(constructDeleteMap(entityDef, id).get());
        } catch (InterruptedException | ExecutionException e) {
            if (callback != null) {
                callback.onException(e);
            }
            return;
        }

        // TODO Delete ManyToOne foreign entities?
        System.out.println(writeMap);
        connection.write(rootPath, writeMap).thenAccept(new Consumer<Void>() {
            @Override
            public void accept(Void aVoid) {
                if (callback != null) {
                    callback.onComplete(null);
                }
            }
        });
    }

    private CompletableFuture<Map<String, Object>> constructDeleteMap(final EntityDefinition entityDefinition,
                                                                      final String id) {
        final CompletableFuture<Map<String, Object>> promise = new CompletableFuture<>();
        final Map<String, Object> writeMap = new HashMap<>();

        find(entityDefinition.getEntityType(), id, new FirebombCallback() {
            @Override
            public void onComplete(Object entity) {

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

            @Override
            public void onException(Exception e) {
                promise.completeExceptionally(e);
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
