package firebomb.definition;

import java.util.HashMap;
import java.util.Map;

public class EntityDefinitionManager {
    private Map<Class, BasicEntityDefinition> basicDefinitionMap = new HashMap<>();
    private Map<Class, EntityDefinition> definitionMap = new HashMap<>();

    public BasicEntityDefinition getBasicDefinition(Class entityType) {
        if (definitionMap.containsKey(entityType)) {
            return definitionMap.get(entityType);
        }

        if (!basicDefinitionMap.containsKey(entityType)) {
            basicDefinitionMap.put(entityType, new BasicEntityDefinition(entityType));
        }

        return basicDefinitionMap.get(entityType);
    }

    public EntityDefinition getDefinition(Class entityType) {
        if (!definitionMap.containsKey(entityType)) {
            definitionMap.put(entityType, new EntityDefinition(entityType));
            if (basicDefinitionMap.containsKey(entityType)) {
                basicDefinitionMap.remove(entityType);
            }
        }

        return definitionMap.get(entityType);
    }
}
