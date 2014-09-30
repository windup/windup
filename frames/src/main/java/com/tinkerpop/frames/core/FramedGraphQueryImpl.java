package com.tinkerpop.frames.core;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedEdgeIterable;
import com.tinkerpop.frames.structures.FramedVertexIterable;

public class FramedGraphQueryImpl implements FramedGraphQuery {
	private GraphQuery graphQuery;
	private FramedGraph<?> graph;

	public FramedGraphQueryImpl(FramedGraph<?> graph, GraphQuery graphQuery) {
		this.graph = graph;
		this.graphQuery = graphQuery;
	}

	public FramedGraphQuery has(String key) {
		graphQuery = graphQuery.has(key);
		return this;
	}

	public FramedGraphQuery hasNot(String key) {
		graphQuery = graphQuery.hasNot(key);
		return this;
	}

	public FramedGraphQuery has(String key, Object value) {
		graphQuery = graphQuery.has(key, value);
		return this;
	}

	public FramedGraphQuery hasNot(String key, Object value) {
		graphQuery = graphQuery.hasNot(key, value);
		return this;
	}

	public FramedGraphQuery has(String key, Predicate predicate, Object value) {
		graphQuery = graphQuery.has(key, predicate, value);
		return this;
	}

	public <T extends Comparable<T>> FramedGraphQuery has(String key, T value,
			Compare compare) {
		graphQuery = graphQuery.has(key, value, compare);
		return this;
	}

	public <T extends Comparable<?>> FramedGraphQuery interval(String key,
			T startValue, T endValue) {
		graphQuery = graphQuery.interval(key, startValue, endValue);
		return this;
	}

	public FramedGraphQuery limit(int limit) {
		graphQuery = graphQuery.limit(limit);
		return this;
	}

	@Override
	public <T> Iterable<T> edges(Class<T> kind) {
		return new FramedEdgeIterable<T>(graph, edges(), kind);
	}

	@Override
	public <T> Iterable<T> vertices(Class<T> kind) {
		return new FramedVertexIterable<T>(graph, vertices(), kind);
	}

	@Override
	public Iterable<Edge> edges() {
		return graphQuery.edges();
	}

	@Override
	public Iterable<Vertex> vertices() {
		return graphQuery.vertices();
	}

}
