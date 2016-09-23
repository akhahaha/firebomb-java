package firebomb;

import firebomb.definition.EntityDefinition;

import java.util.HashMap;
import java.util.Map;

public class EntityDefinitionManager {
    private static EntityDefinitionManager ourInstance = new EntityDefinitionManager();

    public static EntityDefinitionManager getInstance() {
        return ourInstance;
    }

    private Map<Class, EntityDefinition> basicDefinitionMap = new HashMap<>();
    private Map<Class, EntityDefinition> definitionMap = new HashMap<>();

    public EntityDefinition getBasicDefinition(Class entityType) {
        if (basicDefinitionMap.containsKey(entityType)) {
            basicDefinitionMap.put(entityType, new EntityDefinition(entityType));
        }

        return basicDefinitionMap.get(entityType);
    }

    public EntityDefinition getDefinition(Class entityType) {
        if (definitionMap.containsKey(entityType)) {
            definitionMap.put(entityType, new EntityDefinition(entityType));
        }

        return definitionMap.get(entityType);
    }
}
