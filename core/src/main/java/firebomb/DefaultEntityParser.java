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
        // TODO Implement eager loading
        for (ManyToManyDefinition manyToManyDef : entityDef.getManyToManyDefinitions()) {
            List<Object> foreignEntities = new ArrayList<>();
            if (data.child(manyToManyDef.getName()) != null) {
                for (Data foreignEntityData : data.child(manyToManyDef.getName()).getChildren()) {
                    foreignEntities.add(deserializeBasicEntity(manyToManyDef.getForeignEntityDefinition(),
                            foreignEntityData));
                }
            }
            manyToManyDef.set(entity, foreignEntities);
        }

        for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
            if (data.child(manyToOneDef.getName()) != null) {
                manyToOneDef.set(entity, deserializeBasicEntity(manyToOneDef.getForeignEntityDefinition(),
                        data.child(manyToOneDef.getName()).getChildren().get(0)));
            }
        }

        for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
            List<Object> foreignEntities = new ArrayList<>();
            if (data.child(oneToManyDef.getName()) != null) {
                for (Data foreignEntityData : data.child(oneToManyDef.getName()).getChildren()) {
                    foreignEntities.add(deserializeBasicEntity(oneToManyDef.getForeignEntityDefinition(),
                            foreignEntityData));
                }
            }
            oneToManyDef.set(entity, foreignEntities);
        }

        return entity;
    }

    private <T> T deserializeBasicEntity(BasicEntityDefinition entityDef, Data data) {
        T entity;
        try {
            entity = (T) entityDef.getEntityType().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new FirebombException(e);
        }

        // Set ID
        entityDef.setId(entity, data.child(entityDef.getIdName()).getValue(String.class));

        return entity;
    }

    @Override
    public Data serialize(Object entity) {
        EntityDefinition entityDef = entityDefinitionManager.getDefinition(entity.getClass());

        Data rootData = new Data();

        // Add Id
        String idName = entityDef.getIdName();
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
            String foreignIdName = manyToManyDef.getForeignIdName();
            for (Object foreignEntity : manyToManyDef.get(entity)) {
                String foreignId = manyToManyDef.getForeignId(foreignEntity);
                rootData.setValue(StringUtils.path(
                        entityPath, manyToManyDef.getName(), foreignId, foreignIdName), foreignId);
                rootData.setValue(StringUtils.path(
                        manyToManyDef.constructForeignIndexPath(foreignId), entityId, idName), entityId);
            }
        }

        // Add ManyToOne
        for (ManyToOneDefinition manyToOneDef : entityDef.getManyToOneDefinitions()) {
            Object foreignEntity = manyToOneDef.get(entity);
            String foreignIdName = manyToOneDef.getForeignIdName();
            String foreignId = manyToOneDef.getForeignId(foreignEntity);
            rootData.setValue(StringUtils.path(
                    entityPath, manyToOneDef.getName(), foreignId, foreignIdName), foreignId);
            rootData.setValue(StringUtils.path(
                    manyToOneDef.constructForeignIndexPath(foreignId), entityId, idName), entityId);
        }

        // Add OneToMany
        for (OneToManyDefinition oneToManyDef : entityDef.getOneToManyDefinitions()) {
            String foreignIdName = oneToManyDef.getForeignIdName();
            for (Object foreignEntity : oneToManyDef.get(entity)) {
                String foreignId = oneToManyDef.getForeignId(foreignEntity);
                rootData.setValue(StringUtils.path(
                        entityPath, oneToManyDef.getName(), foreignId, foreignIdName), foreignId);
                rootData.setValue(StringUtils.path(
                        oneToManyDef.constructForeignFieldPath(foreignId), entityId, idName), entityId);
            }
        }

        return rootData;
    }
}
