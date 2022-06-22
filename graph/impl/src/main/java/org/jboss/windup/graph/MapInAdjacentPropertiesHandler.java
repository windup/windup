package org.jboss.windup.graph;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import com.syncleus.ferma.WrappedFramedGraph;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Handles @MapInAdjacentProperties Map<String,String>.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class MapInAdjacentPropertiesHandler extends AbstractMethodHandler implements MethodHandler {
    private static final Logger log = Logging.get(MapInAdjacentPropertiesHandler.class);

    @Override
    public Class<MapInAdjacentProperties> getAnnotationType() {
        return MapInAdjacentProperties.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        String methodName = method.getName();
        if (methodName.startsWith("get"))
            return createInterceptor(builder, method);
        else if (methodName.startsWith("set"))
            return createInterceptor(builder, method);

        throw new WindupException("Only get* and set* method names are supported for @" + MapInAdjacentProperties.class.getSimpleName());
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method) {
        return builder.method(ElementMatchers.is(method))
                .intercept(MethodDelegation.to(MapInAdjacentPropertiesHandler.MapInAdjacentPropertiesInterceptor.class));
    }

    public static final class MapInAdjacentPropertiesInterceptor {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments final Object[] args) {
            final MapInAdjacentProperties ann = ((CachesReflection) thisFrame).getReflectionCache().getAnnotation(method, MapInAdjacentProperties.class);

            Element thisElement = thisFrame.getElement();
            if (!(thisElement instanceof Vertex))
                throw new WindupException("Element is not of supported type, must be Vertex, but was: " + thisElement.getClass().getCanonicalName());
            Vertex vertex = (Vertex) thisElement;

            String methodName = method.getName();
            if (methodName.startsWith("get"))
                return handleGetter(vertex, method, args, ann);

            if (methodName.startsWith("set")) {
                handleSetter(vertex, method, args, ann, thisFrame.getGraph());
                return null;
            }

            throw new WindupException("Only get* and set* method names are supported for @" + MapInAdjacentProperties.class.getSimpleName());
        }

        /**
         * Getter
         */
        private static Map<String, Serializable> handleGetter(Vertex vertex, Method method, Object[] args,
                                                              MapInAdjacentProperties ann) {
            if (args != null && args.length != 0)
                throw new WindupException("Method must take no arguments: " + method.getName());

            // Find the map vertex.
            Map<String, Serializable> map = new HashMap<>();
            Iterator<Vertex> it = vertex.vertices(Direction.OUT, ann.label());
            Vertex mapVertex = null;
            if (!it.hasNext()) {
                // No map yet.
                return map;
            } else {
                mapVertex = it.next();
                if (it.hasNext()) {
                    // Multiple vertices behind edges with given label.
                    log.warning("Found multiple vertices for a map, using only first one; for: " + method.getName());
                }
            }

            Set<String> keys = mapVertex.keys();
            for (String key : keys) {
                final Property<Object> val = mapVertex.property(key);
                if (!val.isPresent() || !(val.value() instanceof String))
                    log.warning("@InProperties is meant for Map<String,Serializable>, but the value was: " + val.getClass());
                map.put(key, "" + val.value());
            }
            return map;
        }

        /**
         * Setter
         */

        private static void handleSetter(Vertex vertex, Method method, Object[] args, MapInAdjacentProperties ann,
                                         FramedGraph framedGraph) {
            // Argument.
            if (args == null || args.length != 1)
                throw new WindupException("Method must take one argument: " + method.getName());

            if (!(args[0] instanceof Map))
                throw new WindupException("Argument of " + method.getName() + " must be a Map, but is: " + args[0].getClass());

            if (!(framedGraph instanceof WrappedFramedGraph))
                throw new WindupException("Framed graph must be an instance of " + WrappedFramedGraph.class.getCanonicalName());

            Graph graph = (Graph) ((WrappedFramedGraph) framedGraph).getBaseGraph();

            @SuppressWarnings("unchecked")
            Map<String, Serializable> map = (Map<String, Serializable>) args[0];

            // Find or create the map vertex.
            Iterator<Vertex> it = vertex.vertices(Direction.OUT, ann.label());
            Vertex mapVertex = null;
            if (!it.hasNext()) {
                // No map vertex yet.
                mapVertex = graph.addVertex();
                vertex.addEdge(ann.label(), mapVertex);
            } else {
                mapVertex = it.next();
                if (it.hasNext()) {
                    // Multiple vertices behind edges with given label.
                    log.warning("Found multiple vertices for a map, using only first one; for: " + method.getName());
                }
            }

            // For all keys in the old map...
            Set<String> keys = mapVertex.keys();
            Set<String> mapKeys = map.keySet();
            for (String key : keys) {
                final Property<Object> val = mapVertex.property(key);
                if (!val.isPresent() || !(val.value() instanceof String)) {
                    log.warning("@InProperties is meant for Map<String,Serializable>, but the value was: " + val.getClass());
                }
                // ...either change to new value,
                if (map.containsKey(key)) {
                    mapVertex.property(key, map.get(key));
                    mapKeys.remove(key);
                }
                // or remove the old.
                else
                    val.remove();
            }

            // Add the new entries.
            for (String key : mapKeys) {
                mapVertex.property(key, map.get(key));
            }
        }
    }
}
