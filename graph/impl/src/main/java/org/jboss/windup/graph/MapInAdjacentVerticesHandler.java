package org.jboss.windup.graph;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;

public class MapInAdjacentVerticesHandler implements MethodHandler<MapInAdjacentVertices>
{
    @Override
    public Class<MapInAdjacentVertices> getAnnotationType()
    {
        return MapInAdjacentVertices.class;
    }

    @Override
    public Object processElement(Object frame, Method method, Object[] arguments, MapInAdjacentVertices annotation,
                FramedGraph<?> framedGraph, Element element)
    {
        if (!(element instanceof Vertex))
            throw new WindupException("@" + MapInAdjacentVertices.class.getSimpleName() + " is only supported on Vertexes.");

        String methodName = method.getName();
        if (methodName.startsWith("get"))
        {
            return handleGetter((Vertex) element, method, arguments, annotation, framedGraph);
        }
        else if (methodName.startsWith("set"))
        {
            handleSetter((Vertex) element, method, arguments, annotation, framedGraph);
            return null;
        }

        throw new WindupException("Only get* and set* method names are supported.");
    }

    /**
     * Getter.
     */
    private Map<String, WindupVertexFrame> handleGetter(Vertex vertex, Method method, Object[] arguments,
                MapInAdjacentVertices annotation, FramedGraph<?> framedGraph)
    {
        if (arguments != null && arguments.length != 0)
            throw new WindupException("Method must take zero arguments: " + method.getName());

        Map<String, WindupVertexFrame> result = new HashMap<>();
        Iterable<Edge> edges = vertex.getEdges(Direction.IN, annotation.label());
        for (Edge edge : edges)
        {
            String key = edge.getProperty(annotation.mapKeyField());
            Vertex v = edge.getVertex(Direction.OUT);
            WindupVertexFrame frame = framedGraph.frame(v, WindupVertexFrame.class);
            result.put(key, frame);
        }
        return result;
    }

    /**
     * Setter.
     */
    private void handleSetter(Vertex vertex, Method method, Object[] arguments, MapInAdjacentVertices annotation,
                FramedGraph<?> framedGraph)
    {
        if (arguments == null || arguments.length != 1)
            throw new WindupException("Method must take only one argument: " + method.getName());

        Iterable<Edge> edges = vertex.getEdges(Direction.IN, annotation.label());
        for (Edge edge : edges)
        {
            framedGraph.removeEdge(edge);
        }

        @SuppressWarnings("unchecked")
        Map<String, WindupVertexFrame> map = (Map<String, WindupVertexFrame>) arguments[0];
        for (Map.Entry<String, WindupVertexFrame> entry : map.entrySet())
        {
            Edge edge = framedGraph.addEdge(null, entry.getValue().asVertex(), vertex, annotation.label());
            edge.setProperty(annotation.mapKeyField(), entry.getKey());
        }
    }
}
