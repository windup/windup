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

public class FrameMapHandler implements MethodHandler<AdjacentMap>
{
    @Override
    public Class<AdjacentMap> getAnnotationType()
    {
        return AdjacentMap.class;
    }

    @Override
    public Object processElement(Object frame, Method method, Object[] arguments, AdjacentMap annotation,
                FramedGraph<?> framedGraph, Element element)
    {
        String methodName = method.getName();
        if (methodName.startsWith("get"))
        {
            return handleGetter(element, method, arguments, annotation, framedGraph);
        }
        else if (methodName.startsWith("set"))
        {
            handleSetter(element, method, arguments, annotation, framedGraph);
            return null;
        }
        else
        {
            throw new WindupException("Unsupported method type... only get* and set* method names are supported!");
        }
    }

    private Map<String, WindupVertexFrame> handleGetter(Element element, Method method, Object[] arguments,
                AdjacentMap annotation, FramedGraph<?> framedGraph)
    {
        if (!(element instanceof Vertex))
        {
            throw new WindupException("Error, @AdjacentMap is only supported on Vertex objects");
        }
        else if (arguments != null && arguments.length != 0)
        {
            throw new WindupException("Error, method must take zero arguments");
        }
        else
        {
            Map<String, WindupVertexFrame> result = new HashMap<>();
            Vertex vertex = (Vertex) element;
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
    }

    private void handleSetter(Element element, Method method, Object[] arguments, AdjacentMap annotation,
                FramedGraph<?> framedGraph)
    {
        if (!(element instanceof Vertex))
        {
            throw new WindupException("Error, @AdjacentMap is only supported on Vertex objects");
        }
        else if (arguments == null || arguments.length != 1)
        {
            throw new WindupException("Error, method must take only one argument");
        }
        else
        {
            Vertex vertex = (Vertex) element;
            Iterable<Edge> edges = vertex.getEdges(Direction.OUT, annotation.label());
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
}
