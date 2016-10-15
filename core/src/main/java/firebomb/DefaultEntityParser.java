package firebomb;

import firebomb.database.Data;
import firebomb.definition.*;
import firebomb.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultEntityParser implements EntityParser {
    private static final EntityDefinitionManager entityDefinitionManager = new EntityDefinitionManager();

    @Override
    public <T> T deserialize(Class<T> entityType, Data data) {
        EntityDefinition entityDef = entityDefinitionManager.getDefinition(entityType);

        T entity;
        try {
            entity = (T) entityType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FirebombException(e);
        }

        if (data.getValue() == null && data.getChildren().isEmpty()) {
            return null;
        }

        // Set ID
        entityDef.setId(entity, (String) data.child(entityDef.getIdName()).getValue());

        // Set fields
        for (FieldDefinition fieldDef : entityDef.getFieldDefinitions()) {
            if (data.child(fieldDef.getName()) != null) {
                fieldDef.set(entity, data.child(fieldDef.getName()).getValue(fieldDef.getType()));
                // TODO: Verify lists
            }
        }

        // Set relations
        for (ManyToManyDefinition manyToManyDef : entityDef.getManyToManyDefinitions()) {
            List<Object> foreignEntities = new ArrayList<>();
            if (data.child(manyToManyDef.getName()) != null) {
                for (Data foreignEntityData : data.child(manyToManyDef.getName()).getChildren()) {
                    foreignEntities.add(deserialize(manyToManyDef.getForeignEntityType(), foreignEntityData));
                }
            }
            manyToManyDef.set(entity, foreignEntities);
        }

        for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
            if (data.child(manyToOneDef.getName()) != null) {
                manyToOneDef.set(entity, deserialize(manyToOneDef.getForeignEntityType(),
                        data.child(manyToOneDef.getName()).getChildren().get(0)));
            }
        }

        for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
            List<Object> foreignEntities = new ArrayList<>();
            if (data.child(oneToManyDef.getName()) != null) {
                for (Data foreignEntityData : data.child(oneToManyDef.getName()).getChildren()) {
                    foreignEntities.add(deserialize(oneToManyDef.getForeignEntityType(), foreignEntityData));
                }
            }
            oneToManyDef.set(entity, foreignEntities);
        }

        return entity;
    }

    @Override
    public Data serialize(Object entity) {
        EntityDefinition entityDef = entityDefinitionManager.getDefinition(entity.getClass());

        Data rootData = new Data();

        // Add Id
        String entityId = entityDef.getId(entity);
        String entityPath = StringUtils.path(entityDef.getReference(), entityId);
        rootData.setValue(StringUtils.path(entityPath, entityDef.getIdName()), entityId);

        // Add fields
        for (FieldDefinition fieldDef : entityDef.getFieldDefinitions()) {
            Object fieldValue = fieldDef.get(entity);
            if (fieldDef.isNonNull() && fieldValue == null) {
                throw new FirebombException("Non-null field '" + entityDef.getName() + "." + fieldDef.getName() +
                        "' cannot be null.");
            }
            rootData.setValue(StringUtils.path(entityPath, fieldDef.getName()), fieldValue);
        }

        // Add ManyToMany
        for (ManyToManyDefinition manyToManyDef : entityDef.getManyToManyDefinitions()) {
            for (Object foreignEntity : manyToManyDef.get(entity)) {
                String foreignId = manyToManyDef.getForeignId(foreignEntity);
                rootData = serializeBasicEntity(rootData,
                        StringUtils.path(entityPath, manyToManyDef.getName(), foreignId),
                        manyToManyDef.getForeignEntityDefinition(), foreignEntity);
                rootData = serializeBasicEntity(rootData, StringUtils.path(
                        manyToManyDef.constructForeignIndexPath(foreignId), entityId), entityDef, entity);
            }
        }

        // Add ManyToOne
        for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
            Object foreignEntity = manyToOneDef.get(entity);
            String foreignId = manyToOneDef.getForeignId(foreignEntity);
            rootData = serializeBasicEntity(rootData,
                    StringUtils.path(entityPath, manyToOneDef.getName(), foreignId),
                    manyToOneDef.getForeignEntityDefinition(), foreignEntity);
            rootData = serializeBasicEntity(rootData, StringUtils.path(
                    manyToOneDef.constructForeignIndexPath(foreignId), entityId), entityDef, entity);
        }

        // Add OneToMany
        for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
            for (Object foreignEntity : oneToManyDef.get(entity)) {
                String foreignId = oneToManyDef.getForeignId(foreignEntity);
                rootData = serializeBasicEntity(rootData,
                        StringUtils.path(entityPath, oneToManyDef.getName(), foreignId),
                        oneToManyDef.getForeignEntityDefinition(), foreignEntity);
                rootData = serializeBasicEntity(rootData, StringUtils.path(
                        oneToManyDef.constructForeignFieldPath(foreignId), entityId), entityDef, entity);
            }
        }

        return rootData;
    }

    private Data serializeBasicEntity(Data rootData, String path, BasicEntityDefinition entityDefinition,
                                      Object entity) {
        rootData.setValue(StringUtils.path(path, entityDefinition.getIdName()), entityDefinition.getId(entity));
        for (FieldDefinition fieldDefinition : entityDefinition.getFieldDefinitions()) {
            rootData.setValue(StringUtils.path(path, fieldDefinition.getName()), fieldDefinition.get(entity));
        }

        return rootData;
    }
}
