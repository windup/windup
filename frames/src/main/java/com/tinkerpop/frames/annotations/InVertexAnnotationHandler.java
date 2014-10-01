package com.tinkerpop.frames.annotations;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.InVertex;

public class InVertexAnnotationHandler implements AnnotationHandler<InVertex> {
    @Override
    public Class<InVertex> getAnnotationType() {
        return InVertex.class;
    }

    @Override
    public Object processElement(final InVertex annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Element element, final Direction direction) {
        if (element instanceof Edge) {
            return framedGraph.frame(((Edge)element).getVertex(Direction.IN), method.getReturnType());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
