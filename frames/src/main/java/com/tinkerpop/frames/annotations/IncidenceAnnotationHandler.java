package com.tinkerpop.frames.annotations;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.Incidence;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.structures.FramedEdgeIterable;

public class IncidenceAnnotationHandler implements AnnotationHandler<Incidence> {

    @Override
    public Class<Incidence> getAnnotationType() {
        return Incidence.class;
    }

    @Override
    public Object processElement(final Incidence annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Element element, final Direction direction) {
        if (element instanceof Vertex) {
            return processVertex(annotation, method, arguments, framedGraph, (Vertex) element);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Object processVertex(final Incidence incidence, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Vertex element) {
        if (ClassUtilities.isGetMethod(method)) {
            return new FramedEdgeIterable(framedGraph, element.getEdges(incidence.direction(), incidence.label()), incidence.direction(), ClassUtilities.getGenericClass(method));
        } else if (ClassUtilities.isAddMethod(method)) {
            
            switch(incidence.direction()) {
            case OUT:
                return framedGraph.addEdge(null, element, ((VertexFrame) arguments[0]).asVertex(), incidence.label(), Direction.OUT, method.getReturnType());
            case IN:
                return framedGraph.addEdge(null, ((VertexFrame) arguments[0]).asVertex(), element, incidence.label(), Direction.IN, method.getReturnType());
            case BOTH:
                throw new UnsupportedOperationException("Direction.BOTH it not supported on 'add' or 'set' methods");
            }
                
        } else if (ClassUtilities.isRemoveMethod(method)) {
            framedGraph.removeEdge(((EdgeFrame) arguments[0]).asEdge());
            return null;
        }

        return null;
    }

}
