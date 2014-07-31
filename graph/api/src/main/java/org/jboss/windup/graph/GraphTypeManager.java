package org.jboss.windup.graph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.graph.model.WindupVertexFrame;


/**
 * Windup's implementation of extended type handling for TinkerPop Frames.
 * Frames can only hold one type per Vertex.
 * This allows storing multiple types based on the @TypeValue.value(),
 * also in the "type" property (see {@link WindupVertexFrame.TYPE_PROP}, but delimited by |.
 */
@Singleton
public class GraphTypeManager implements TypeResolver, FrameInitializer
{
    public static final String DELIMITER = "|";

    private Set<Class<? extends WindupVertexFrame>> registeredTypes = new HashSet<>();
    private TypeRegistry typeRegistry = new TypeRegistry();

    
    public Set<Class<? extends WindupVertexFrame>> getRegisteredTypes()
    {
        return Collections.unmodifiableSet(this.registeredTypes);
    }

    
    public void addTypeToRegistry(Class<? extends WindupVertexFrame> wvf)
    {
        if (wvf.getAnnotation(TypeValue.class) != null)
        {
            // Do not attempt to add items where this is null... we use
            // *Model types with no TypeValue to function as essentially
            // "abstract" models that would never exist on their own (only as subclasses).
            this.typeRegistry.add(wvf);
            this.registeredTypes.add(wvf);
        }
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
        Class<?> typeHoldingTypeField = this.typeRegistry.getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;

        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
        String typeValue = typeValueAnnotation.value();
        if (typeValue.contains(DELIMITER))
        {
            throw new IllegalArgumentException("Type value for class '" + kind.getCanonicalName()
                        + "' must not contain the \"" + DELIMITER + "\" character: " + typeValue);
        }
        if (!StringUtils.isAlphanumeric(typeValue))
        {
            throw new IllegalArgumentException("Type value for class '" + kind.getCanonicalName()
                        + "' must be alphanumeric: " + typeValue);
        }

        // Store the type value in a delimited list.
        String currentPropertyValue = element.getProperty(typeFieldName);
        if (currentPropertyValue == null)
        {
            // If there is no current value, initialize with "|typeValue|".
            element.setProperty(typeFieldName, DELIMITER + typeValue + DELIMITER);
        }
        else if (!currentPropertyValue.contains(DELIMITER + typeValue + DELIMITER))
        {
            // Otherwise, append to the end, making sure that the list is terminated with the delimiter.
            element.setProperty(typeFieldName, currentPropertyValue + typeValue + DELIMITER);
        }

        this.addSuperclassType(kind, element);
    }

    @SuppressWarnings("unchecked")
    private void addSuperclassType(Class<? extends VertexFrame> kind, Element element)
    {
        for (Class<?> superInterface : kind.getInterfaces())
        {
            if (WindupVertexFrame.class.isAssignableFrom(superInterface))
            {
                addTypeToElement((Class<? extends VertexFrame>) superInterface, element);
            }
        }

    }

    /**
     * Returns the classes which this edge represents, typically subclasses.
     */
    @Override
    public Class<?>[] resolveTypes(Edge e, Class<?> defaultType)
    {
        return resolve(e, defaultType);
    }

    /**
     * Returns the classes which this vertex represents, typically subclasses.
     */
    @Override
    public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType)
    {
        return resolve(v, defaultType);
    }

    /**
     * Returns the classes which this vertex/edge represents, typically subclasses. Always appends
     */
    private Class<?>[] resolve(Element e, Class<?> defaultType)
    {
        // The class field holding the name of the type holding property.
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(defaultType);
        if (typeHoldingTypeField != null)
        {
            // Name of the graph element property holding the type list.
            String propName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
            String valuesAll = e.getProperty(propName);
            if (valuesAll != null)
            {
                String[] valuesArray = StringUtils.split(valuesAll, DELIMITER);
                List<Class<?>> resultClasses = new ArrayList<>();
                for (String value : valuesArray)
                {
                    Class<?> type = typeRegistry.getType(typeHoldingTypeField, value);
                    if (type == null)
                        continue;
                    resultClasses.add(type);
                }
                if (!resultClasses.isEmpty())
                {
                    resultClasses.add(VertexFrame.class);
                    return resultClasses.toArray(new Class<?>[resultClasses.size()]);
                }
            }
        }
        return new Class[] { defaultType, VertexFrame.class };
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element)
    {
        if (VertexFrame.class.isAssignableFrom(kind))
        {
            addTypeToElement((Class<? extends VertexFrame>) kind, element);
        }
    }
}
