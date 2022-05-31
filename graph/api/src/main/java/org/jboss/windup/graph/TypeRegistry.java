package org.jboss.windup.graph;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.util.exception.WindupException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class TypeRegistry {
    Map<String, Class<?>> typeDiscriminators = new HashMap<>();

    public Class<?> getType(String typeName) {
        return typeDiscriminators.get(typeName);
    }

    /**
     * @param type Add the interface to the registry. The interface should have a {@link TypeValue} annotation, and there should be a
     *             {@link TypeField} annotation on the interface or its parents.
     */
    public TypeRegistry add(Class<? extends WindupFrame> type) {
        String typeString = GraphTypeManager.getTypeValue(type);
        if (typeString == null)
            throw new WindupException(String.format("The type does not have a @TypeValue annotation: %s", type.getName()));
        typeDiscriminators.put(typeString, type);
        return this;
    }
}
