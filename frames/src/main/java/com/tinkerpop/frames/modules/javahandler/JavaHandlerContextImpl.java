package com.tinkerpop.frames.modules.javahandler;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Implementation for java handler context.
 * 
 * @author Bryn Cooke
 * 
 */
class JavaHandlerContextImpl<C extends Element> implements JavaHandlerContext<C> {

	private final FramedGraph<?> graph;
	private final Method method;
	private final C context;

	JavaHandlerContextImpl(FramedGraph<?> graph, Method method, C context) {
		super();
		this.graph = graph;
		this.method = method;
		this.context = context;
	}

	/**
	 * @return The framed graph
	 */
	public FramedGraph<?> g() {
		return graph;
	}

	/**
	 * @return The element that was framed
	 */
	public C it() {
		return context;
	}

	/**
	 * @return A gremlin pipeline at the context element
	 */
	public <E> GremlinPipeline<C, E> gremlin() {
		return new GremlinPipeline<C, E>(it());
	}

	/**
	 * Start a gremlin pipeline
	 * 
	 * @param starts
	 * @return Start a gremlin pipeline at an element
	 */
	public <E> GremlinPipeline<C, E> gremlin(Object starts) {
		return new GremlinPipeline<C, E>(starts);
	}

	/**
	 * Frame a vertex using the return type of the method
	 * 
	 * @param vertex The vertex to frame
	 * @return The framed vertex
	 */
	public <T> T frame(Vertex vertex) {
		return g().frame(vertex, (Class<T>) method.getReturnType());
	}

	/**
	 * Frame a vertex using an explicit kind of frame
	 * 
	 * @param vertex The vertex to frame
	 * @param kind The type of frame
	 * @return The framed vertex
	 */
	public <T> T frame(Vertex vertex, Class<T> kind) {
		return g().frame(vertex, kind);
	}

	/**
	 * Frame an edge using the return type of the method
	 * 
	 * @param edge The edge to frame
	 * @param direction The direction of the edge
	 * @return The framed edge
	 */
	public <T> T frame(Edge edge, Direction direction) {
		return g().frame(edge, direction, (Class<T>) method.getReturnType());
	}

	/**
	 * Frame an edge using an explicit kind of frame
	 * 
	 * @param edge The edge to frame
	 * @param direction The direction of the edge
	 * @param kind The type of frame
	 * @return The framed edge
	 */
	public <T> T frame(Edge edge, Direction direction, Class<T> kind) {
		return (T) g().frame(edge, direction, kind);
	}

	/**
	 * Frame some vertices using the return type of the method
	 * 
	 * @param vertices The vertices to frame
	 * @return The framed vertices
	 */
	public <T> Iterable<T> frameVertices(Iterable<Vertex> vertices) {
		Type type = getIterableType();
		
		return g().frameVertices(vertices, (Class<T>) type);
	}



	/**
	 * Frame some vertices using an explicit kind of frame
	 * 
	 * @param vertices The vertices to frame
	 * @param kind The kind of frame
	 * @return The framed vertices
	 */
	public <T> Iterable<T> frameVertices(Iterable<Vertex> vertices, Class<T> kind) {
		return (Iterable<T>) g().frameVertices(vertices, kind);
	}

	/**
	 * Frame some edges using the return type of the method
	 * 
	 * @param edges the edges to frame
	 * @param direction The direction of the edges
	 * @return The framed edges
	 */
	public <T> Iterable<T> frameEdges(Iterable<Edge> edges, Direction direction) {
		Type type = getIterableType();
		return g().frameEdges(edges, direction, (Class<T>) type);
	}

	/**
	 * Frame some edges using an explicit kind of frame
	 * 
	 * @param edges the edges to frame
	 * @param direction The direction of the edges
	 * @param kind The kind of frame
	 * @return The framed edges
	 */
	public <T> Iterable<T> frameEdges(Iterable<Edge> edges, Direction direction, Class<T> kind) {
		return (Iterable<T>) g().frameEdges(edges, direction, kind);
	}

	
	private Type getIterableType() {
		
		if(method.getReturnType() != Iterable.class) {
			throw new JavaHandlerException("Method return type is not iterable: " + method);
		}
		Type genericReturnType = method.getGenericReturnType();
		if(!(genericReturnType instanceof ParameterizedType)) {
			throw new JavaHandlerException("Method must specify generic parameter for Iterable: " + method);	
		}
		return ClassUtilities.getGenericClass(method);
	}

	@Override
	public <T> T frame(Edge edge) {
		return g().frame(edge, (Class<T>) method.getReturnType());
	}

	
	@Override
	public <T> T frame(Edge edge, Class<T> kind) {
		return g().frame(edge, kind);
	}
	
	@Override
	public <T> Iterable<T> frameEdges(Iterable<Edge> edges) {
		Type type = getIterableType();
		return g().frameEdges(edges, (Class<T>) type);
	}

	@Override
	public <T> Iterable<T> frameEdges(Iterable<Edge> edges, Class<T> kind) {
		return g().frameEdges(edges, kind);
	}

	

}