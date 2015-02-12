package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.graphdb.vertices.StandardVertex;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Windup's implementation of extended type handling for TinkerPop Frames. This allows storing multiple types based on the @TypeValue.value(), also in
 * the type property (see {@link WindupVertexFrame.TYPE_PROP}.
 */
@Singleton
public class GraphTypeManager implements TypeResolver, FrameInitializer
{
    private Map<String, Class<? extends WindupVertexFrame>> registeredTypes = new HashMap<>();
    private TypeRegistry typeRegistry = new TypeRegistry();

    public Set<Class<? extends WindupVertexFrame>> getRegisteredTypes()
    {
        return Collections.unmodifiableSet(new HashSet<>(registeredTypes.values()));
    }

    public void addTypeToRegistry(Class<? extends WindupVertexFrame> wvf)
    {
        TypeValue typeValueAnnotation = wvf.getAnnotation(TypeValue.class);

        // Do not attempt to add items where this is null... we use
        // *Model types with no TypeValue to function as essentially
        // "abstract" models that would never exist on their own (only as subclasses).
        if (typeValueAnnotation != null)
        {
            if (registeredTypes.containsKey(typeValueAnnotation.value()))
            {
                throw new IllegalArgumentException("Type value for model '" + wvf.getCanonicalName()
                            + "' is already registered with model "
                            + registeredTypes.get(typeValueAnnotation.value()).getName());
            }
            registeredTypes.put(typeValueAnnotation.value(), wvf);
            typeRegistry.add(wvf);
        }
    }

    /**
     * Remove the given type from the provided {@link Element}.
     */
    public void removeTypeFromElement(Class<? extends VertexFrame> kind, Element element)
    {
        StandardVertex v = GraphTypeManager.asTitanVertex(element);
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;

        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
        String typeValue = typeValueAnnotation.value();
        v.removeProperty(typeFieldName);

        for (TitanProperty existingType : v.getProperties(typeFieldName))
        {
            if (!existingType.getValue().toString().equals(typeValue))
            {
                v.addProperty(typeFieldName, existingType.getValue());
            }
        }

        v.addProperty(typeFieldName, typeValue);
        addSuperclassType(kind, element);
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends VertexFrame> kind, Element element)
    {
        StandardVertex v = GraphTypeManager.asTitanVertex(element);
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;

        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
        String typeValue = typeValueAnnotation.value();

        for (TitanProperty existingType : v.getProperties(typeFieldName))
        {
            if (existingType.getValue().toString().equals(typeValue))
            {
                // this is already in the list, so just exit now
                return;
            }
        }

        v.addProperty(typeFieldName, typeValue);
        addSuperclassType(kind, element);
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

    public static boolean hasType(Class<? extends WindupVertexFrame> type, WindupVertexFrame frame)
    {
        return hasType(type, frame.asVertex());
    }

    public static boolean hasType(Class<? extends WindupVertexFrame> type, Vertex v)
    {
        TypeValue typeValueAnnotation = type.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
        {
            throw new IllegalArgumentException("Class " + type.getCanonicalName() + " lacks a @TypeValue annotation");
        }
        StandardVertex titanVertex = GraphTypeManager.asTitanVertex(v);
        Iterable<TitanProperty> vertexTypes = titanVertex.getProperties(WindupVertexFrame.TYPE_PROP);
        for (TitanProperty typeProp : vertexTypes)
        {
            String typeValue = typeProp.getValue().toString();
            if (typeValue.equals(typeValueAnnotation.value()))
            {
                return true;
            }
        }
        return false;
    }

    private static StandardVertex asTitanVertex(Element e)
    {
        if (e instanceof StandardVertex)
        {
            return (StandardVertex) e;
        }
        else if (e instanceof EventVertex)
        {
            return (StandardVertex) ((EventVertex) e).getBaseVertex();
        }
        else
        {
            throw new IllegalArgumentException("Unrecognized element type: " + e.getClass());
        }
    }

    /**
     * Returns the classes which this vertex/edge represents, typically subclasses. This will only return the lowest level subclasses (no superclasses
     * of types in the type list will be returned). This prevents Annotation resolution issues between superclasses and subclasses (see also:
     * WINDUP-168).
     */
    private Class<?>[] resolve(Element e, Class<?> defaultType)
    {
        // The class field holding the name of the type holding property.
        Class<?> typeHoldingTypeField = typeRegistry.getTypeHoldingTypeField(defaultType);
        if (typeHoldingTypeField != null)
        {
            // Name of the graph element property holding the type list.
            String propName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
            StandardVertex v = GraphTypeManager.asTitanVertex(e);

            Iterable<TitanProperty> valuesAll = v.getProperties(propName);
            if (valuesAll != null)
            {

                List<Class<?>> resultClasses = new ArrayList<>();
                for (TitanProperty value : valuesAll)
                {
                    Class<?> type = typeRegistry.getType(typeHoldingTypeField, value.getValue().toString());
                    if (type != null)
                    {
                        // first check that no subclasses have already been added
                        ListIterator<Class<?>> previouslyAddedIterator = resultClasses.listIterator();
                        boolean shouldAdd = true;
                        while (previouslyAddedIterator.hasNext())
                        {
                            Class<?> previouslyAdded = previouslyAddedIterator.next();
                            if (previouslyAdded.isAssignableFrom(type))
                            {
                                // Remove the previously added superclass
                                previouslyAddedIterator.remove();
                            }
                            else if (type.isAssignableFrom(previouslyAdded))
                            {
                                // The current type is a superclass of a previously added type, don't add it
                                shouldAdd = false;
                            }
                        }

                        if (shouldAdd)
                        {
                            resultClasses.add(type);
                        }
                    }
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
