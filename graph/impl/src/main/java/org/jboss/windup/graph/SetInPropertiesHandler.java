package org.jboss.windup.graph;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.framefactories.annotation.PropertyMethodHandler;
import com.syncleus.ferma.framefactories.annotation.ReflectionUtility;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;

/**
 * Handles @SetInProperties Set<String,String>.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class SetInPropertiesHandler extends AbstractMethodHandler implements MethodHandler {
    private static final String SET_VERTEX_PROP_VALUE = "1";

    @Override
    public Class<SetInProperties> getAnnotationType() {
        return SetInProperties.class;
    }

    /**
     * The handling method.
     */
    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        String methodName = method.getName();
        if (ReflectionUtility.isGetMethod(method))
            return createInterceptor(builder, method);
        else if (ReflectionUtility.isSetMethod(method))
            return createInterceptor(builder, method);
        else if (methodName.startsWith("addAll"))
            return createInterceptor(builder, method);
        else if (methodName.startsWith("add"))
            return createInterceptor(builder, method);
        else
            throw new WindupException("Only get*, set*, add*, and addAll* method names are supported for @"
                    + SetInProperties.class.getSimpleName() + ", found at: " + method.getName());
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(SetInPropertiesHandler.SetInPropertiesMethodInterceptor.class));
    }

    public static final class SetInPropertiesMethodInterceptor {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments final Object[] args) {
            final SetInProperties ann = ((CachesReflection) thisFrame).getReflectionCache().getAnnotation(method, SetInProperties.class);

            Element thisElement = thisFrame.getElement();
            if (!(thisElement instanceof Vertex))
                throw new WindupException("Element is not of supported type, must be Vertex, but was: " + thisElement.getClass().getCanonicalName());
            Vertex vertex = (Vertex) thisElement;

            String methodName = method.getName();
            if (methodName.startsWith("get"))
                return handleGetter(vertex, method, args, ann);

            else if (methodName.startsWith("set"))
                handleSetter(vertex, method, args, ann);

            else if (methodName.startsWith("addAll"))
                handleAddAll(vertex, method, args, ann);

            else if (methodName.startsWith("add"))
                handleAdder(vertex, method, args, ann);

            else
                throw new WindupException("Only get*, set*, add*, and addAll* method names are supported for @"
                        + SetInProperties.class.getSimpleName() + ", found at: " + method.getName());

            return thisFrame;
        }

        /**
         * Getter
         */
        private static Set<String> handleGetter(Vertex vertex, Method method, Object[] args, SetInProperties ann) {
            if (args != null && args.length != 0)
                throw new WindupException("Method must take zero arguments");

            Set<String> set = new HashSet<>();
            String prefix = preparePrefix(ann);

            Set<String> keys = vertex.keys();
            for (String key : keys) {
                String tail = key;
                if (!prefix.isEmpty()) {
                    if (!key.startsWith(prefix))
                        continue;
                    else
                        tail = key.substring(prefix.length());
                }

                set.add(tail);
            }

            return set;
        }

        /**
         * Setter
         */
        private static void handleSetter(Vertex vertex, Method method, Object[] args, SetInProperties ann) {
            // Argument.
            if (args == null || args.length != 1)
                throw new WindupException("Method must take one argument: " + method.getName());

            if (!(args[0] instanceof Set))
                throw new WindupException("Argument of " + method.getName() + " must be a Set<String>, but is: " + args[0].getClass());

            @SuppressWarnings("unchecked")
            Set<String> newSet = (Set<String>) args[0];

            String prefix = preparePrefix(ann);

            // For all keys in the old set...
            Set<String> vertKeys = vertex.keys();
            for (String vertKey : vertKeys) {
                if (!vertKey.startsWith(prefix))
                    continue;
                if (WindupVertexFrame.TYPE_PROP.equals(vertKey)) // Leave the "type" property.
                    continue;
                if (vertKey.startsWith("w:")) // Leave windup internal properties. TODO: Get the prefix from somewhere.
                    continue;

                String subKey = vertKey.substring(prefix.length());
                // ...either change to the new value,
                if (newSet.contains(subKey)) {
                    vertex.property(vertKey, SET_VERTEX_PROP_VALUE);
                    newSet.remove(subKey);
                }
                // or remove the old.
                else
                    vertex.property(vertKey).remove();
            }

            // Add the new entries.
            for (String item : newSet) {
                if (!(item instanceof String))
                    throw new WindupException("Argument of " + method.getName() + " must be a Set<String>, but it contains: " + item.getClass());
                vertex.property(prefix + item, "1");
            }
        }

        private static void handleAdder(Vertex vertex, Method method, Object[] args, SetInProperties ann) {
            if (args == null || args.length == 0)
                throw new WindupException("Method must take at least one String argument: " + method.getName());

            String prefix = preparePrefix(ann);

            for (Object arg : args) {
                if (!(arg instanceof String))
                    throw new WindupException("The arguments of the add*() method " + method.getName() + " must be String, but was: " + arg.getClass());

                vertex.property(prefix + arg, SET_VERTEX_PROP_VALUE);
            }
        }

        /**
         * Adder
         */
        private static void handleAddAll(Vertex vertex, Method method, Object[] args, SetInProperties ann) {
            if (args == null || args.length != 1)
                throw new WindupException("Method must take one String argument: " + method.getName());

            String prefix = preparePrefix(ann);

            // Argument.
            @SuppressWarnings("unchecked")
            Set<String> set = (Set<String>) args[0];

            // Store all set entries in vertex'es properties.
            for (String item : set) {
                vertex.property(prefix + item, SET_VERTEX_PROP_VALUE);
            }
        }

        /**
         * Returns "&lt;ann.propertyPrefix()>&lt;SEPAR>", for example, "set:"; or an empty string if the prefix is empty.
         */
        private static String preparePrefix(SetInProperties ann) {
            return "".equals(ann.propertyPrefix()) ? "" : (ann.propertyPrefix() + SetInProperties.SEPAR);
        }
    }
}
