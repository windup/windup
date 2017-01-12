package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.thinkaurelius.titan.graphdb.internal.AbstractElement;
import com.thinkaurelius.titan.graphdb.relations.StandardEdge;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdge;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import com.thinkaurelius.titan.core.TitanProperty;
import com.thinkaurelius.titan.graphdb.vertices.StandardVertex;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.modules.typedgraph.TypeField;
import com.tinkerpop.frames.modules.typedgraph.TypeRegistry;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.logging.Logger;

/**
 * Windup's implementation of extended type handling for TinkerPop Frames. This allows storing multiple types based on the @TypeValue.value(), also in
 * the type property (see {@link WindupVertexFrame#TYPE_PROP}.
 */
public class GraphTypeManager implements TypeResolver, FrameInitializer
{
    private static final Logger LOG = Logger.getLogger(GraphTypeManager.class.getName());

    private Map<String, Class<? extends WindupFrame<?>>> registeredTypes;
    private TypeRegistry typeRegistry;

    public GraphTypeManager()
    {
    }

    private void initRegistry()
    {
        Furnace furnace = SimpleContainer.getFurnace(GraphTypeManager.class.getClassLoader());
        FurnaceClasspathScanner furnaceClasspathScanner = furnace.getAddonRegistry().getServices(FurnaceClasspathScanner.class).get();

        this.registeredTypes = new HashMap<>();
        this.typeRegistry = new TypeRegistry();
        GraphModelScanner.loadFrames(furnaceClasspathScanner).forEach(this::addTypeToRegistry);
    }

    public Set<Class<? extends WindupFrame<?>>> getRegisteredTypes()
    {
        return Collections.unmodifiableSet(new HashSet<>(getRegisteredTypeMap().values()));
    }

    private synchronized Map<String, Class<? extends WindupFrame<?>>> getRegisteredTypeMap()
    {
        if (registeredTypes == null)
            initRegistry();
        return registeredTypes;
    }

    private synchronized TypeRegistry getTypeRegistry()
    {
        if (typeRegistry == null)
            initRegistry();
        return typeRegistry;
    }

    private void addTypeToRegistry(Class<? extends WindupFrame<?>> frameType)
    {
        LOG.info(" Adding type to registry: " + frameType.getName());

        TypeValue typeValueAnnotation = frameType.getAnnotation(TypeValue.class);

        // Do not attempt to add types without @TypeValue. We use
        // *Model types with no @TypeValue to function as essentially
        // "abstract" models that would never exist on their own (only as subclasses).
        if (typeValueAnnotation == null)
        {
            String msg = String.format("@%s is missing on type %s", TypeValue.class.getSimpleName(), frameType.getName());
            LOG.warning(msg);
            return;
        }

        if (getRegisteredTypeMap().containsKey(typeValueAnnotation.value()))
        {
            throw new IllegalArgumentException("Type value for model '" + frameType.getCanonicalName()
                        + "' is already registered with model "
                        + getRegisteredTypeMap().get(typeValueAnnotation.value()).getName());
        }
        getRegisteredTypeMap().put(typeValueAnnotation.value(), frameType);
        getTypeRegistry().add(frameType);
    }

    /**
     * Remove the given type from the provided {@link Element}.
     */
    public void removeTypeFromElement(Class<? extends WindupFrame<?>> kind, Element element)
    {
        Class<?> typeHoldingTypeField = getTypeRegistry().getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;
        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();


        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;
        String typeValue = typeValueAnnotation.value();

        AbstractElement abstractElement = GraphTypeManager.asTitanElement(element);

        List<String> newTypes = new ArrayList<>();
        for (String existingType : (Iterable<String>)abstractElement.getProperty(typeFieldName))
        {
            if (!existingType.toString().equals(typeValue))
            {
                newTypes.add(typeValue);
            }
        }
        abstractElement.removeProperty(typeFieldName);
        for (String newType : newTypes)
            addProperty(abstractElement, typeFieldName, newType);

        addSuperclassType(kind, element);
    }

    private void addProperty(AbstractElement abstractElement, String propertyName, String propertyValue)
    {
        if (abstractElement instanceof StandardVertex)
            ((StandardVertex) abstractElement).addProperty(propertyName, propertyValue);
        else
        {
            List<String> existingList = abstractElement.getProperty(propertyName);
            if (existingList == null)
            {
                abstractElement.setProperty(propertyName, Collections.singletonList(propertyValue));
            }
            else
            {
                List<String> newList = new ArrayList<>(existingList);
                newList.add(propertyValue);
                abstractElement.setProperty(propertyName, propertyValue);
            }
        }
    }

    /**
     * Returns the type identifier for given type - the value in the property discriminating this type.
     */
    public static String getTypeIdentifier(Class<? extends VertexFrame> modelInterface)
    {
        TypeValue typeValueAnnotation = modelInterface.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return null;

        return typeValueAnnotation.value();
    }

    /**
     * Adds the type value to the field denoting which type the element represents. This is similar
     * to {@link GraphTypeManager#addTypeToElement(Class, Element)}, however it uses a String type instead. The
     * String type will be looked up from the type registry to determine the class type to use.
     */
    public void addTypeToElement(String typeString, Element element)
    {
        Class<? extends WindupFrame<?>> kind = getRegisteredTypeMap().get(typeString);
        if (kind == null)
            throw new IllegalArgumentException("Unrecognized type: " + typeString);

        addTypeToElement(kind, element);
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends WindupFrame<?>> kind, Element element)
    {
        Class<?> typeHoldingTypeField = getTypeRegistry().getTypeHoldingTypeField(kind);
        if (typeHoldingTypeField == null)
            return;

        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeFieldName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
        String typeValue = typeValueAnnotation.value();

        AbstractElement abstractElement = GraphTypeManager.asTitanElement(element);
        for (String existingType : (Iterable<String>)abstractElement.getProperty(typeFieldName))
        {
            if (existingType.equals(typeValue))
            {
                // this is already in the list, so just exit now
                return;
            }
        }

        addProperty(abstractElement, typeFieldName, typeValue);
        addSuperclassType(kind, element);
    }

    @SuppressWarnings("unchecked")
    private void addSuperclassType(Class<? extends WindupFrame<?>> kind, Element element)
    {
        for (Class<?> superInterface : kind.getInterfaces())
        {
            if (WindupFrame.class.isAssignableFrom(superInterface))
            {
                addTypeToElement((Class<? extends WindupFrame<?>>) superInterface, element);
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
        AbstractElement abstractElement= GraphTypeManager.asTitanElement(v);
        Iterable<String> vertexTypes = abstractElement.getProperty(WindupVertexFrame.TYPE_PROP);
        for (String typeValue : vertexTypes)
        {
            if (typeValue.equals(typeValueAnnotation.value()))
            {
                return true;
            }
        }
        return false;
    }

    public static AbstractElement asTitanElement(Element e)
    {
        if (e instanceof StandardVertex)
        {
            return (StandardVertex) e;
        }
        else if (e instanceof EventVertex)
        {
            return (AbstractElement) ((EventVertex) e).getBaseVertex();
        } else if (e instanceof StandardEdge)
        {
            return (StandardEdge)e;
        } else if (e instanceof EventEdge)
        {
            return (AbstractElement) ((EventEdge) e).getBaseEdge();
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
        Class<?> typeHoldingTypeField = getTypeRegistry().getTypeHoldingTypeField(defaultType);
        if (typeHoldingTypeField != null)
        {
            // Name of the graph element property holding the type list.
            String propName = typeHoldingTypeField.getAnnotation(TypeField.class).value();
            AbstractElement abstractElement = GraphTypeManager.asTitanElement(e);

            Iterable<String> valuesAll = abstractElement.getProperty(propName);
            if (valuesAll != null)
            {

                List<Class<?>> resultClasses = new ArrayList<>();
                for (String value : valuesAll)
                {
                    Class<?> type = getTypeRegistry().getType(typeHoldingTypeField, value);
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
        if (VertexFrame.class.isAssignableFrom(kind) || EdgeFrame.class.isAssignableFrom(kind))
        {
            addTypeToElement((Class<? extends WindupFrame<?>>) kind, element);
        }
    }

    /**
     * Build TinkerPop Frames module - a collection of models.
     */
    public Module build()
    {
        return new AbstractModule()
        {
            @Override
            public void doConfigure(FramedGraphConfiguration config)
            {
                config.addTypeResolver(GraphTypeManager.this);
                config.addFrameInitializer(GraphTypeManager.this);
            }
        };
    }
}
