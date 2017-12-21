package org.jboss.windup.graph;

import com.syncleus.ferma.ClassInitializer;
import com.syncleus.ferma.EdgeFrame;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.typeresolvers.TypeResolver;
import com.thinkaurelius.titan.core.TitanEdge;
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
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import com.thinkaurelius.titan.graphdb.vertices.StandardVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import java.util.Arrays;
import java.util.logging.Logger;
import org.jboss.windup.util.exception.WindupException;

/**
 * Windup's implementation of extended type handling for TinkerPop Frames. This allows storing multiple types based on the @TypeValue.value(), also in
 * the type property (see {@link WindupVertexFrame#TYPE_PROP}.
 */
public class GraphTypeManager implements TypeResolver, ClassInitializer
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

    public Class<? extends WindupFrame> getTypeForDiscriminator(String discriminator)
    {
        return this.getRegisteredTypeMap().get(discriminator);
    }

    public Set<Class<? extends WindupFrame<?>>> getRegisteredTypes()
    {
        return Collections.unmodifiableSet(new HashSet<>(getRegisteredTypeMap().values()));
    }

    /**
     * Returns the type discriminator value for given Frames model class, extracted from the @TypeValue annotation.
     */
    public static String getTypeValue(Class<? extends WindupFrame> clazz)
    {
        TypeValue typeValueAnnotation = clazz.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " lacks a @TypeValue annotation");

        return typeValueAnnotation.value();
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
        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;
        String typeValue = typeValueAnnotation.value();

        AbstractElement abstractElement = GraphTypeManager.asTitanElement(element);

        List<String> newTypes = new ArrayList<>();
        for (String existingType : (Iterable<String>)abstractElement.property(WindupFrame.TYPE_PROP))
        {
            if (!existingType.toString().equals(typeValue))
            {
                newTypes.add(typeValue);
            }
        }
        abstractElement.property(WindupFrame.TYPE_PROP).remove();
        for (String newType : newTypes)
            addProperty(abstractElement, WindupFrame.TYPE_PROP, newType);

        addSuperclassType(kind, element);
    }

    private void addProperty(AbstractElement abstractElement, String propertyName, String propertyValue)
    {
        // This uses the direct Titan API which is indexed. See GraphContextImpl.
        if (abstractElement instanceof StandardVertex)
            ((StandardVertex) abstractElement).property(propertyName, propertyValue);
        // StandardEdge doesn't have addProperty().
        else if (abstractElement instanceof StandardEdge)
            //((StandardEdge) abstractElement).setProperty(propertyName, propertyValue);
            addTokenProperty(abstractElement, propertyName, propertyValue);
        // For all others, we resort to storing a list
        else
        {
            Property<List<String>> property = abstractElement.property(propertyName);
            if (property == null)
            {
                abstractElement.property(propertyName, Collections.singletonList(propertyValue));
            }
            else
            {
                List<String> existingList = property.value();
                List<String> newList = new ArrayList<>(existingList);
                newList.add(propertyValue);
                abstractElement.property(propertyName, newList);
            }
        }
    }

    private void addTokenProperty(AbstractElement el, String propertyName, String propertyValue)
    {
        Property<String> val = el.property(propertyName);
        if (val == null)
            el.property(propertyName, propertyValue);
        else
            el.property(propertyName, val.value() + "|" + propertyValue);
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends WindupFrame<?>> kind, Element element)
    {
        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeValue = typeValueAnnotation.value();

        AbstractElement abstractElement = GraphTypeManager.asTitanElement(element);
        Property<Object> typeProp = abstractElement.property(WindupFrame.TYPE_PROP);
        if (typeProp != null)
        {
            if (!(typeProp instanceof Iterable))
                throw new RuntimeException("Discriminators property is not Iterable, but " + typeProp.getClass() + ": " + typeProp);

            for (String existingType : (Iterable<String>)typeProp.value())
            {
                if (existingType.equals(typeValue))
                {
                    // this is already in the list, so just exit now
                    return;
                }
            }
        }

        addProperty(abstractElement, WindupFrame.TYPE_PROP, typeValue);
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

    public static boolean hasType(Class<? extends WindupVertexFrame> type, Vertex v)
    {
        TypeValue typeValueAnnotation = type.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
        {
            throw new IllegalArgumentException("Class " + type.getCanonicalName() + " lacks a @TypeValue annotation");
        }
        AbstractElement abstractElement= GraphTypeManager.asTitanElement(v);
        Iterable<String> vertexTypes = (Iterable<String>)abstractElement.property(WindupVertexFrame.TYPE_PROP).value();
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
        } else if (e instanceof StandardEdge)
        {
            return (StandardEdge)e;
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
    @Override
    public <T> Class<T> resolve(Element e, Class<T> defaultType)
    {
        AbstractElement abstractElement = GraphTypeManager.asTitanElement(e);

        final Property<Object> typeValue = abstractElement.property(WindupFrame.TYPE_PROP);
        if (typeValue == null)
            return defaultType;

        Iterable<String> valuesAll = null;

        if (abstractElement instanceof StandardVertex)
        {
            if (!Iterable.class.isAssignableFrom(typeValue.getClass()))
                throw new WindupException(String.format("Expected Iterable stored in vertex's %s, was %s: %s", WindupFrame.TYPE_PROP, typeValue.getClass().getName(), typeValue.toString()));
            valuesAll = (Iterable<String>) typeValue;
        }
        else if (abstractElement instanceof TitanEdge)
        {
            if (!String.class.isAssignableFrom(typeValue.getClass()))
                throw new WindupException(String.format("Expected String with tokens stored in edge's %s, was %s: %s", WindupFrame.TYPE_PROP, typeValue.getClass().getName(), typeValue.toString()));
            valuesAll = Arrays.asList(((String)typeValue.value()).split("|"));
        }
        else
            throw new WindupException(String.format("Unknown element type: %s", abstractElement.getClass().getName()));


        List<Class<?>> resultClasses = new ArrayList<>();
        for (String value : valuesAll)
        {
            Class<?> type = getTypeRegistry().getType(value);
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
                    resultClasses.add(type);
            }
        }
        if (!resultClasses.isEmpty())
        {
            resultClasses.add(VertexFrame.class);
            return (Class<T>)resultClasses.get(0);
        }
        return defaultType;
    }

    @Override
    public Class<?> getInitializationType()
    {
        return null;
    }

    @Override
    public void initalize(Object frame)
    {
    }

    @Override
    public Class<?> resolve(Element element)
    {
        return null;
    }

    @Override
    public void init(Element element, Class<?> kind)
    {
        if (VertexFrame.class.isAssignableFrom(kind) || EdgeFrame.class.isAssignableFrom(kind))
        {
            addTypeToElement((Class<? extends WindupFrame<?>>) kind, element);
        }
    }

    @Override
    public void deinit(Element element)
    {

    }

    @Override
    public <P extends Element, T extends Element> GraphTraversal<P, T> hasType(GraphTraversal<P, T> traverser, Class<?> type)
    {
        return null;
    }

    @Override
    public <P extends Element, T extends Element> GraphTraversal<P, T> hasNotType(GraphTraversal<P, T> traverser, Class<?> type)
    {
        return null;
    }
}
