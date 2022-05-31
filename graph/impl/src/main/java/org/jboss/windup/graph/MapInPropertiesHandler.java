package org.jboss.windup.graph;

import com.syncleus.ferma.ElementFrame;
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
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Handles @MapInProperties Map<String,String>.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class MapInPropertiesHandler extends AbstractMethodHandler implements MethodHandler {
    private static final Logger log = Logging.get(MapInPropertiesHandler.class);

    @Override
    public Class<MapInProperties> getAnnotationType() {
        return MapInProperties.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        String methodName = method.getName();
        if (methodName.startsWith("get"))
            return createInterceptor(builder, method);

        if (methodName.startsWith("set"))
            return createInterceptor(builder, method);

        if (methodName.startsWith("put"))
            return createInterceptor(builder, method);

        if (methodName.startsWith("putAll"))
            return createInterceptor(builder, method);

        throw new WindupException("Only get*, set*, and put* method names are supported for @"
                + MapInProperties.class.getSimpleName() + ", found at: " + method.getName());
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(MapInPropertiesHandler.MapInPropertiesInterceptor.class));
    }

    public static final class MapInPropertiesInterceptor {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments final Object[] args) {
            final MapInProperties ann = ((CachesReflection) thisFrame).getReflectionCache().getAnnotation(method, MapInProperties.class);

            Element thisElement = thisFrame.getElement();
            if (!(thisElement instanceof Vertex))
                throw new WindupException("Element is not of supported type, must be Vertex, but was: " + thisElement.getClass().getCanonicalName());
            Vertex vertex = (Vertex) thisElement;

            String methodName = method.getName();
            if (methodName.startsWith("get"))
                return handleGetter(vertex, method, args, ann);

            if (methodName.startsWith("set"))
                return handleSetter(vertex, method, args, ann);

            if (methodName.startsWith("put"))
                return handleAdder(vertex, method, args, ann);

            if (methodName.startsWith("putAll"))
                return handleAdder(vertex, method, args, ann);

            throw new WindupException("Only get*, set*, and put* method names are supported for @"
                    + MapInProperties.class.getSimpleName() + ", found at: " + method.getName());
        }

        /**
         * Getter
         */
        private static Map<String, Object> handleGetter(Vertex vertex, Method method, Object[] args, MapInProperties ann) {
            if (args != null && args.length != 0)
                throw new WindupException("Method must take zero arguments");

            Map<String, Object> map = new HashMap<>();
            String prefix = preparePrefix(ann);

            Set<String> keys = vertex.keys();
            for (String key : keys) {
                if (!key.startsWith(prefix))
                    continue;

                // Skip the type property
                if (key.equals(WindupFrame.TYPE_PROP))
                    continue;

                final Property<Object> val = vertex.property(key);
                if (!ann.propertyType().isAssignableFrom(val.value().getClass())) {
                    log.warning("@InProperties is meant for Map<String," + ann.propertyType().getName() + ">, but the value was: " + val.getClass());
                }

                map.put(key.substring(prefix.length()), val.value());
            }

            return map;
        }

        /**
         * Setter
         */
        private static WindupVertexFrame handleSetter(Vertex vertex, Method method, Object[] args, MapInProperties ann) {
            // Argument.
            if (args == null || args.length != 1)
                throw new WindupException("Method must take one argument: " + method.getName());

            if (!(args[0] instanceof Map))
                throw new WindupException("Argument of " + method.getName() + " must be a Map, but is: " + args[0].getClass());

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) args[0];

            String prefix = preparePrefix(ann);

            // For all keys in the old map...
            Set<String> keys = vertex.keys();
            Set<String> mapKeys = map.keySet();
            for (String key : keys) {
                if (!key.startsWith(prefix))
                    continue;
                if (WindupVertexFrame.TYPE_PROP.equals(key)) // Leave the "type" property.
                    continue;
                if (key.startsWith("w:")) // Leave windup internal properties. TODO: Get the prefix from somewhere.
                    continue;

                final Property<Object> val = vertex.property(key);
                if (!ann.propertyType().isAssignableFrom(val.value().getClass())) {
                    log.warning("@InProperties is meant for Map<String," + ann.propertyType().getName() + ">, but the value was: " + val.getClass());
                }
                String subKey = key.substring(prefix.length());
                // ...either change to new value,
                if (map.containsKey(subKey)) {
                    vertex.property(key, map.get(subKey));
                    mapKeys.remove(subKey);
                }
                // or remove the old.
                else
                    vertex.property(key).remove();
            }

            // Add the new entries.
            for (String key : mapKeys) {
                vertex.property(prefix + key, map.get(key));
            }

            return null;
        }

        /**
         * Adder
         */
        private static WindupVertexFrame handleAdder(Vertex vertex, Method method, Object[] args, MapInProperties ann) {
            if (args != null && args.length != 1)
                throw new WindupException("Method '" + method.getName() + "' must take one argument, not " + args.length);

            if (args == null || args[0] == null || !(args[0] instanceof Map))
                throw new WindupException("Method '" + method.getName() + "' must take one argument, " +
                        "a Map<String, Serializable> to store in the vertex. Was: "
                        + (args == null || args[0] == null ? "null" : args[0].getClass()));

            String prefix = preparePrefix(ann);

            // Argument.
            @SuppressWarnings("unchecked")
            Map<String, Serializable> map = (Map<String, Serializable>) args[0];

            // Store all map entries in vertex'es properties.
            for (Map.Entry<String, Serializable> entry : map.entrySet()) {
                final Object value = entry.getValue();
                if (!(value instanceof Serializable))
                    throw new WindupException("The values of the map to store in a vertex must all implement Serializable.");
                vertex.property(prefix + entry.getKey(), value);
            }

            return null;
        }

        /**
         * Returns "<ann.propertyPrefix()><SEPAR>", for example, "map:".
         */
        private static String preparePrefix(MapInProperties ann) {
            return "".equals(ann.propertyPrefix()) ? "" : (ann.propertyPrefix() + MapInProperties.SEPAR);
        }

    }
}
