package org.jboss.windup.graph.frames;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.core.FramedGraphQueryImpl;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

public class TypeAwareFramedGraphQuery extends FramedGraphQueryImpl {

	public TypeAwareFramedGraphQuery(FramedGraph<?> graph) {
		super(graph, graph.getBaseGraph().query());
	}

	public <T extends Comparable<T>> FramedGraphQuery type(Class<? extends WindupVertexFrame> kind) {
		return this.has(WindupVertexFrame.TYPE_PROP, getTypeValue(kind));
	}

    /**
     * Returns the type discriminator value for given Frames model class, extracted from the @TypeValue annotation.
     */
    public static String getTypeValue(Class<? extends WindupVertexFrame> clazz)
    {
        TypeValue typeValueAnnotation = clazz.getAnnotation(TypeValue.class);
        if (typeValueAnnotation == null)
            throw new IllegalArgumentException("Class " + clazz.getCanonicalName() + " lacks a @TypeValue annotation");

        return typeValueAnnotation.value();
    }
}
