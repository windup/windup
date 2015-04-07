package com.tinkerpop.frames.annotations;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.structures.FramedVertexIterable;

import java.lang.reflect.Method;

public class AdjacencyAnnotationHandler implements AnnotationHandler<Adjacency> {

    @Override
    public Class<Adjacency> getAnnotationType() {
        return Adjacency.class;
    }

    @Override
    public Object processElement(final Adjacency annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph,
            final Element element, final Direction direction) {
        if (element instanceof Vertex) {
            return processVertex(annotation, method, arguments, framedGraph, (Vertex) element);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Object processVertex(final Adjacency adjacency, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Vertex vertex) {
        if (ClassUtilities.isGetMethod(method)) {
            final FramedVertexIterable r = new FramedVertexIterable(framedGraph, vertex.getVertices(adjacency.direction(), adjacency.label()),
                    ClassUtilities.getGenericClass(method));
            if (ClassUtilities.returnsIterable(method)) {
                return r;
            } else {
                return r.iterator().hasNext() ? r.iterator().next() : null;
            }
        } else if (ClassUtilities.isAddMethod(method)) {
            Class<?> returnType = method.getReturnType();
            Vertex newVertex;
            Object returnValue = null;
            if (arguments == null) {
                // Use this method to get the vertex so that the vertex
                // initializer is called.
                returnValue = framedGraph.addVertex(null, returnType);
                newVertex = ((VertexFrame) returnValue).asVertex();
            } else {
                if (arguments[0] == null)
                    throw new IllegalArgumentException("null passed to @Adjacency " + method.getName() + " labelled " + adjacency.label());
                newVertex = ((VertexFrame) arguments[0]).asVertex();
            }
            addEdges(adjacency, framedGraph, vertex, newVertex);

            if (returnType.isPrimitive()) {
                return null;
            } else {
                return returnValue;
            }

        } else if (ClassUtilities.isRemoveMethod(method)) {
            removeEdges(adjacency.direction(), adjacency.label(), vertex, ((VertexFrame) arguments[0]).asVertex(), framedGraph);
            return null;
        } else if (ClassUtilities.isSetMethod(method)) {
            removeEdges(adjacency.direction(), adjacency.label(), vertex, null, framedGraph);
            if (ClassUtilities.acceptsIterable(method)) {
                for (Object o : (Iterable) arguments[0]) {
                    Vertex v = ((VertexFrame) o).asVertex();
                    addEdges(adjacency, framedGraph, vertex, v);
                }
                return null;
            } else {
                if (null != arguments[0]) {
                    Vertex newVertex = ((VertexFrame) arguments[0]).asVertex();
                    addEdges(adjacency, framedGraph, vertex, newVertex);
                }
                return null;
            }
        }

        return null;
    }

    private void addEdges(final Adjacency adjacency, final FramedGraph framedGraph, final Vertex vertex, Vertex newVertex) {
        switch(adjacency.direction()) {
        case OUT:
            framedGraph.addEdge(null, vertex, newVertex, adjacency.label());
            break;
        case IN:
            framedGraph.addEdge(null, newVertex, vertex, adjacency.label());
            break;
        case BOTH:
            throw new UnsupportedOperationException("Direction.BOTH it not supported on 'add' or 'set' methods");
        }
    }

    private void removeEdges(final Direction direction, final String label, final Vertex element, final Vertex otherVertex, final FramedGraph framedGraph) {
        for (final Edge edge : element.getEdges(direction, label)) {
            if (null == otherVertex || edge.getVertex(direction.opposite()).equals(otherVertex)) {
                framedGraph.removeEdge(edge);
            }
        }
    }
}