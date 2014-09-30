package com.tinkerpop.frames.annotations;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.Range;

import java.lang.reflect.Method;

public class RangeAnnotationHandler implements AnnotationHandler<Range> {

    @Override
    public Class<Range> getAnnotationType() {
        return Range.class;
    }

    @Override
    public Object processElement(final Range annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Element element, final Direction direction) {
        if (element instanceof Edge) {
            return processEdge(annotation, method, arguments, framedGraph, (Edge) element, direction);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public Object processEdge(final Range annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Edge edge, final Direction direction) {
        return framedGraph.frame(edge.getVertex(direction.opposite()), method.getReturnType());
    }

}
