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
		TypeValue typeValue = kind.getAnnotation(TypeValue.class);
		   if (typeValue == null) {
	            throw new IllegalArgumentException("Must be annotated with 'TypeValue': " + kind.getName());
		   }
		return this.has(WindupVertexFrame.TYPE_PROP, typeValue.value());
	}
}
