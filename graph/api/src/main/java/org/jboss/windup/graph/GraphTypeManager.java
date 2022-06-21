package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraphEdge;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.furnace.FurnaceClasspathScanner;

import com.syncleus.ferma.EdgeFrame;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.typeresolvers.TypeResolver;

import net.bytebuddy.ByteBuddy;

/**
 * Windup's implementation of extended type handling for TinkerPop Frames. This allows storing multiple types based on the @TypeValue.value(), also in
 * the type property (see {@link WindupVertexFrame#TYPE_PROP}.
 */
public class GraphTypeManager implements TypeResolver {
    private static final Logger LOG = Logger.getLogger(GraphTypeManager.class.getName());

    private GraphApiCompositeClassLoaderProvider graphApiCompositeClassLoaderProvider;
    private Furnace furnace;
    private Map<String, Class<? extends WindupFrame<?>>> registeredTypes;
    private TypeRegistry typeRegistry;
    private Map<String, Class<?>> classCache = new HashMap<>();

    public GraphTypeManager() {
    }

    /**
     * Returns the type discriminator value for given Frames model class, extracted from the @TypeValue annotation.
     */
    public static String getTypeValue(Class<? extends WindupFrame> clazz) {
        TypeValue typeValueAnnotation = clazz.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " lacks a @TypeValue annotation");

        return typeValueAnnotation.value();
    }

    private static Set<String> getTypeProperties(Element abstractElement) {
        Set<String> results = new HashSet<>();
        Iterator<? extends Property> properties = null;
        if (abstractElement instanceof Vertex) {
            // LOG.info("Getting from standardvertex as properties method");
            properties = ((Vertex) abstractElement).properties(WindupFrame.TYPE_PROP);
        } else if (abstractElement instanceof JanusGraphEdge) {
            Property<String> typeProperty = abstractElement.property(WindupFrame.TYPE_PROP);
            if (typeProperty.isPresent()) {
                List<String> all = Arrays.asList(((String) typeProperty.value()).split("\\|"));
                results.addAll(all);
                return results;
            }
        } else {
            // LOG.info("Using the old style properties method");
            properties = Collections.singleton(abstractElement.property(WindupFrame.TYPE_PROP)).iterator();
        }

        if (properties == null)
            return results;

        properties.forEachRemaining(property -> {
            if (property.isPresent())
                results.add((String) property.value());
        });
        return results;
    }

    public static boolean hasType(Class<? extends WindupVertexFrame> type, Vertex v) {
        TypeValue typeValueAnnotation = type.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null) {
            throw new IllegalArgumentException("Class " + type.getCanonicalName() + " lacks a @TypeValue annotation");
        }
        // LOG.info("has type called for: " + type + " and vertex: " + v);
        Iterable<String> vertexTypes = getTypeProperties(v);
        for (String typeValue : vertexTypes) {
            if (typeValue.equals(typeValueAnnotation.value())) {
                return true;
            }
        }
        return false;
    }

    private GraphApiCompositeClassLoaderProvider getGraphApiCompositeClassLoaderProvider() {
        if (this.graphApiCompositeClassLoaderProvider == null)
            this.graphApiCompositeClassLoaderProvider = getFurnace().getAddonRegistry().getServices(GraphApiCompositeClassLoaderProvider.class)
                    .get();
        return this.graphApiCompositeClassLoaderProvider;
    }

    private Furnace getFurnace() {
        if (furnace == null)
            this.furnace = SimpleContainer.getFurnace(GraphContextFactory.class.getClassLoader());
        return this.furnace;
    }

    private void initRegistry() {
        FurnaceClasspathScanner furnaceClasspathScanner = getFurnace().getAddonRegistry().getServices(FurnaceClasspathScanner.class).get();

        this.registeredTypes = new HashMap<>();
        this.typeRegistry = new TypeRegistry();
        GraphModelScanner.loadFrames(furnaceClasspathScanner).forEach(this::addTypeToRegistry);
    }

    public Class<? extends WindupFrame> getTypeForDiscriminator(String discriminator) {
        return this.getRegisteredTypeMap().get(discriminator);
    }

    public Set<Class<? extends WindupFrame<?>>> getRegisteredTypes() {
        return Collections.unmodifiableSet(new HashSet<>(getRegisteredTypeMap().values()));
    }

    private synchronized Map<String, Class<? extends WindupFrame<?>>> getRegisteredTypeMap() {
        if (registeredTypes == null)
            initRegistry();
        return registeredTypes;
    }

    private synchronized TypeRegistry getTypeRegistry() {
        if (typeRegistry == null)
            initRegistry();
        return typeRegistry;
    }

    private void addTypeToRegistry(Class<? extends WindupFrame<?>> frameType) {
        LOG.info(" Adding type to registry: " + frameType.getName());

        TypeValue typeValueAnnotation = frameType.getAnnotation(TypeValue.class);

        // Do not attempt to add types without @TypeValue. We use
        // *Model types with no @TypeValue to function as essentially
        // "abstract" models that would never exist on their own (only as subclasses).
        if (typeValueAnnotation == null) {
            String msg = String.format("@%s is missing on type %s", TypeValue.class.getSimpleName(), frameType.getName());
            LOG.warning(msg);
            return;
        }

        if (getRegisteredTypeMap().containsKey(typeValueAnnotation.value())) {
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
    public void removeTypeFromElement(Class<? extends WindupFrame<?>> kind, Element element) {
        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;
        String typeValue = typeValueAnnotation.value();

        List<String> newTypes = new ArrayList<>();
        for (String existingType : getTypeProperties(element)) {
            if (!existingType.equals(typeValue)) {
                newTypes.add(typeValue);
            }
        }
        element.properties(WindupFrame.TYPE_PROP).forEachRemaining(Property::remove);
        for (String newType : newTypes)
            addProperty(element, WindupFrame.TYPE_PROP, newType);

        addSuperclassType(kind, element);
    }

    private void addProperty(Element abstractElement, String propertyName, String propertyValue) {
        // This uses the direct Titan API which is indexed. See GraphContextImpl.
        if (abstractElement instanceof Vertex)
            ((Vertex) abstractElement).property(propertyName, propertyValue);
            // StandardEdge doesn't have addProperty().
        else if (abstractElement instanceof Edge)
            addTokenProperty(abstractElement, propertyName, propertyValue);
            // For all others, we resort to storing a list
        else {
            Property<List<String>> property = abstractElement.property(propertyName);
            if (property == null) {
                abstractElement.property(propertyName, Collections.singletonList(propertyValue));
            } else {
                List<String> existingList = property.value();
                List<String> newList = new ArrayList<>(existingList);
                newList.add(propertyValue);
                abstractElement.property(propertyName, newList);
            }
        }
    }

    private void addTokenProperty(Element el, String propertyName, String propertyValue) {
        Property<String> val = el.property(propertyName);
        if (!val.isPresent())
            el.property(propertyName, propertyValue);
        else
            el.property(propertyName, val.value() + "|" + propertyValue);
    }

    /**
     * Adds the type value to the field denoting which type the element represents.
     */
    public void addTypeToElement(Class<? extends WindupFrame<?>> kind, Element element) {
        TypeValue typeValueAnnotation = kind.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            return;

        String typeValue = typeValueAnnotation.value();

        Set<String> types = getTypeProperties(element);

        // LOG.info("Adding type to element: " + element + " type: " + kind + " property is already present? " + types);
        for (String typePropertyValue : types) {
            if (typePropertyValue.equals(typeValue)) {
                // this is already in the list, so just exit now
                return;
            }
        }

        addProperty(element, WindupFrame.TYPE_PROP, typeValue);
        addSuperclassType(kind, element);
    }

    @SuppressWarnings("unchecked")
    private void addSuperclassType(Class<? extends WindupFrame<?>> kind, Element element) {
        for (Class<?> superInterface : kind.getInterfaces()) {
            if (WindupFrame.class.isAssignableFrom(superInterface)) {
                addTypeToElement((Class<? extends WindupFrame<?>>) superInterface, element);
            }
        }
    }

    /**
     * Returns the classes which this vertex/edge represents, typically subclasses. This will only return the lowest level subclasses (no superclasses
     * of types in the type list will be returned). This prevents Annotation resolution issues between superclasses and subclasses (see also:
     * WINDUP-168).
     */
    @Override
    public <T> Class<T> resolve(Element e, Class<T> defaultType) {
        final Set<String> valuesAll = getTypeProperties(e);
        if (valuesAll == null || valuesAll.isEmpty())
            return defaultType;

        List<Class<?>> resultClasses = new ArrayList<>();
        resultClasses.add(defaultType);

        for (String value : valuesAll) {
            Class<?> type = getTypeRegistry().getType(value);
            if (type != null) {
                // first check that no subclasses have already been added
                ListIterator<Class<?>> previouslyAddedIterator = resultClasses.listIterator();
                boolean shouldAdd = true;
                while (previouslyAddedIterator.hasNext()) {
                    Class<?> previouslyAdded = previouslyAddedIterator.next();
                    if (previouslyAdded.isAssignableFrom(type)) {
                        // Remove the previously added superclass
                        previouslyAddedIterator.remove();
                    } else if (type.isAssignableFrom(previouslyAdded)) {
                        // The current type is a superclass of a previously added type, don't add it
                        shouldAdd = false;
                    }
                }

                if (shouldAdd) {
                    resultClasses.add(type);
                }
            }
        }
        if (!resultClasses.isEmpty()) {
            // Ferma needs a single class, so create a composite one
            return (Class<T>) getClass(resultClasses);
        }
        return defaultType;
    }

    private Class<?> getClass(List<Class<?>> interfaces) {
        List<String> interfaceNames = interfaces.stream()
                .map(Class::getCanonicalName)
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        String key = interfaceNames.toString();

        Class<?> result = classCache.get(key);
        if (result == null) {
            result = new ByteBuddy()
                    .makeInterface()
                    .implement(interfaces).make()
                    .load(this.getGraphApiCompositeClassLoaderProvider().getCompositeClassLoader())
                    .getLoaded();
            classCache.put(key, result);
        }
        return result;
    }

    @Override
    public Class<?> resolve(Element element) {
        return resolve(element, WindupFrame.class);
    }

    @Override
    public void init(Element element, Class<?> kind) {
        if (VertexFrame.class.isAssignableFrom(kind) || EdgeFrame.class.isAssignableFrom(kind)) {
            addTypeToElement((Class<? extends WindupFrame<?>>) kind, element);
        }
    }

    @Override
    public void deinit(Element element) {
        element.properties(WindupFrame.TYPE_PROP).forEachRemaining(Property::remove);
    }

    @Override
    public <P extends Element, T extends Element> GraphTraversal<P, T> hasType(GraphTraversal<P, T> traverser, Class<?> type) {
        String typeValue = getTypeValue((Class<? extends WindupFrame>) type);
        return traverser.has(WindupFrame.TYPE_PROP, org.apache.tinkerpop.gremlin.process.traversal.P.eq(typeValue));
    }

    @Override
    public <P extends Element, T extends Element> GraphTraversal<P, T> hasNotType(GraphTraversal<P, T> traverser, Class<?> type) {
        String typeValue = getTypeValue((Class<? extends WindupFrame>) type);
        return traverser.filter(new Predicate<Traverser<T>>() {
            @Override
            public boolean test(final Traverser<T> toCheck) {
                final Property<String> property = toCheck.get().property(WindupFrame.TYPE_PROP);
                if (!property.isPresent())
                    return true;

                final String resolvedType = property.value();
                if (typeValue.contains(resolvedType))
                    return false;
                else
                    return true;
            }
        });
    }
}
