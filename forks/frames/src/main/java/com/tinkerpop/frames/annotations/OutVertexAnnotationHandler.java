package com.tinkerpop.frames.annotations;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.OutVertex;

public class OutVertexAnnotationHandler implements AnnotationHandler<OutVertex> {
    @Override
    public Class<OutVertex> getAnnotationType() {
        return OutVertex.class;
    }

    @Override
    public Object processElement(final OutVertex annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph, final Element element, final Direction direction) {
        if (element instanceof Edge) {
        	return framedGraph.frame(((Edge)element).getVertex(Direction.OUT), method.getReturnType());
        } else {
            throw new UnsupportedOperationException();
        }
    }
}
