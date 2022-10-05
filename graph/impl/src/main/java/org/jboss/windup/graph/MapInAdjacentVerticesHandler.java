package org.jboss.windup.graph;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.TEdge;
import com.syncleus.ferma.VertexFrame;
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

public class MapInAdjacentVerticesHandler extends AbstractMethodHandler implements MethodHandler {
    @Override
    public Class<MapInAdjacentVertices> getAnnotationType() {
        return MapInAdjacentVertices.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        String methodName = method.getName();
        if (methodName.startsWith("get")) {
            return createInterceptor(builder, method);
        } else if (methodName.startsWith("set")) {
            return createInterceptor(builder, method);
        }

        throw new WindupException("Only get* and set* method names are supported.");
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method) {
        return builder.method(ElementMatchers.is(method))
                .intercept(MethodDelegation.to(MapInAdjacentVerticesHandler.MapInAdjacentVerticesInterceptor.class));
    }

    public static final class MapInAdjacentVerticesInterceptor {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments final Object[] args) {
            final MapInAdjacentVertices ann = ((CachesReflection) thisFrame).getReflectionCache().getAnnotation(method, MapInAdjacentVertices.class);

            Element thisElement = thisFrame.getElement();
            if (!(thisElement instanceof Vertex))
                throw new WindupException("Element is not of supported type, must be Vertex, but was: " + thisElement.getClass().getCanonicalName());
            Vertex vertex = (Vertex) thisElement;

            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                return handleGetter(vertex, method, args, ann, thisFrame.getGraph());
            } else if (methodName.startsWith("set")) {
                handleSetter((VertexFrame) thisFrame, method, args, ann, thisFrame.getGraph());
                return null;
            }

            throw new WindupException("Only get* and set* method names are supported.");
        }

        /**
         * Getter.
         */
        private static Map<String, WindupVertexFrame> handleGetter(Vertex vertex, Method method, Object[] arguments,
                                                                   MapInAdjacentVertices annotation, FramedGraph framedGraph) {
            if (arguments != null && arguments.length != 0)
                throw new WindupException("Method must take zero arguments: " + method.getName());

            Map<String, WindupVertexFrame> result = new HashMap<>();
            Iterator<Edge> edges = vertex.edges(Direction.IN, annotation.label());
            while (edges.hasNext()) {
                Edge edge = edges.next();
                Property<String> property = edge.property(annotation.mapKeyField());
                if (property == null)
                    continue;

                Vertex v = edge.outVertex();
                WindupVertexFrame frame = framedGraph.frameElement(v, WindupVertexFrame.class);
                result.put(property.value(), frame);
            }
            return result;
        }

        /**
         * Setter.
         */
        private static void handleSetter(VertexFrame vertexFrame, Method method, Object[] arguments, MapInAdjacentVertices annotation,
                                         FramedGraph framedGraph) {
            if (arguments == null || arguments.length != 1)
                throw new WindupException("Method must take only one argument: " + method.getName());

            Iterator<Edge> edges = vertexFrame.getElement().edges(Direction.IN, annotation.label());
            while (edges.hasNext()) {
                Edge edge = edges.next();
                edge.remove();
            }

            @SuppressWarnings("unchecked")
            Map<String, WindupVertexFrame> map = (Map<String, WindupVertexFrame>) arguments[0];
            for (Map.Entry<String, WindupVertexFrame> entry : map.entrySet()) {
                TEdge edge = framedGraph.addFramedEdge(entry.getValue(), vertexFrame, annotation.label());
                edge.setProperty(annotation.mapKeyField(), entry.getKey());
            }
        }
    }
}
