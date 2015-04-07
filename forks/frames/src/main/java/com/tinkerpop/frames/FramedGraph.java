package com.tinkerpop.frames;

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.frames.annotations.AdjacencyAnnotationHandler;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.annotations.DomainAnnotationHandler;
import com.tinkerpop.frames.annotations.InVertexAnnotationHandler;
import com.tinkerpop.frames.annotations.IncidenceAnnotationHandler;
import com.tinkerpop.frames.annotations.OutVertexAnnotationHandler;
import com.tinkerpop.frames.annotations.PropertyMethodHandler;
import com.tinkerpop.frames.annotations.RangeAnnotationHandler;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovyAnnotationHandler;
import com.tinkerpop.frames.core.FramedGraphQueryImpl;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.TypeResolver;
import com.tinkerpop.frames.structures.FramedEdgeIterable;
import com.tinkerpop.frames.structures.FramedVertexIterable;

/**
 * The primary class for interpreting/framing elements of a graph in terms of
 * particulate annotated interfaces. This is a wrapper graph in that it requires
 * an underlying graph from which to add functionality. The standard Blueprints
 * graph methods are exposed along with extra methods to make framing easy.
 * 
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedGraph<T extends Graph> implements Graph, WrapperGraph<T> {

	protected final T baseGraph;

	private FramedGraphConfiguration config;
	private boolean configViaFactory;

	/**
	 * @param baseGraph The original graph being framed.
	 * @param config The configuration for the framed graph.
	 * @param config.getConfiguredGraph() The graph being framed after module configuration.
	 */
	protected FramedGraph(T baseGraph, FramedGraphConfiguration config) {
		this.config = config;
		this.baseGraph = baseGraph;
		configViaFactory = true;
	}

	/**
	 * Construct a FramedGraph that will frame the elements of the underlying
	 * graph.
	 * 
	 * @param baseGraph
	 *            the graph whose elements to frame
	 * @deprecated Use {@link FramedGraphFactory}.
	 */
	public FramedGraph(final T baseGraph) {
		this.baseGraph = baseGraph;
		config = new FramedGraphConfiguration();
		config.setConfiguredGraph(baseGraph);
		configViaFactory = false;
		config.addMethodHandler(new PropertyMethodHandler());
		registerAnnotationHandler(new AdjacencyAnnotationHandler());
		registerAnnotationHandler(new IncidenceAnnotationHandler());
		registerAnnotationHandler(new DomainAnnotationHandler());
		registerAnnotationHandler(new RangeAnnotationHandler());
		registerAnnotationHandler(new InVertexAnnotationHandler());
		registerAnnotationHandler(new OutVertexAnnotationHandler());
		registerAnnotationHandler(new GremlinGroovyAnnotationHandler());
	}


	/**
	 * A helper method for framing a vertex. Note that all framed vertices
	 * implement {@link VertexFrame} to allow access to the underlying element
	 * 
	 * @param vertex
	 *            the vertex to frame
	 * @param kind
	 *            the default annotated interface to frame the vertex as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy objects backed by a vertex and interpreted from the
	 *         perspective of the annotate interface or null if the vertex parameter was null
	 */
	public <F> F frame(final Vertex vertex, final Class<F> kind) {
		if(vertex == null) {
			return null;
		}
		
		Collection<Class<?>> resolvedTypes = new HashSet<Class<?>>();
		resolvedTypes.add(VertexFrame.class);
		resolvedTypes.add(kind);
		for (TypeResolver typeResolver : config.getTypeResolvers()) {
			resolvedTypes.addAll(Arrays.asList(typeResolver.resolveTypes(
					vertex, kind)));
		}
		return (F) Proxy.newProxyInstance(config.getFrameClassLoaderResolver().resolveClassLoader(kind),
				resolvedTypes.toArray(new Class[resolvedTypes.size()]),
				new FramedElement(this, vertex));
	}

	/**
	 * A helper method for framing an edge. Note that all framed edges implement
	 * {@link EdgeFrame} to allow access to the underlying element
	 * 
	 * @param edge
	 *            the edge to frame
	 * @param direction
	 *            the direction of the edges
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy objects backed by an edge and interpreted from the
	 *         perspective of the annotate interface or null if the edge paramenter was null
	 *         
	 * @deprecated Use {@link #frame(Edge, Class)}, in combination with {@link InVertex} and {@link OutVertex}.
	 */
	public <F> F frame(final Edge edge, final Direction direction,
			final Class<F> kind) {
		
		if(edge == null) {
			return null;
		}
		
		Collection<Class<?>> resolvedTypes = new HashSet<Class<?>>();
		resolvedTypes.add(EdgeFrame.class);
		resolvedTypes.add(kind);
		for (TypeResolver typeResolver : config.getTypeResolvers()) {
			resolvedTypes.addAll(Arrays.asList(typeResolver.resolveTypes(edge,
					kind)));
		}
		return (F) Proxy.newProxyInstance(config.getFrameClassLoaderResolver().resolveClassLoader(kind),
				resolvedTypes.toArray(new Class[resolvedTypes.size()]),
				new FramedElement(this, edge, direction));
	}
	
	/**
	 * A helper method for framing an edge. Note that all framed edges implement
	 * {@link EdgeFrame} to allow access to the underlying element.
	 * 
	 * @param edge
	 *            the edge to frame
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy objects backed by an edge and interpreted from the
	 *         perspective of the annotate interface or null if the edge paramenter was null
	 */
	public <F> F frame(final Edge edge, final Class<F> kind) {
		return frame(edge, Direction.OUT, kind);
	}

	/**
	 * A helper method for framing an iterable of vertices.
	 * 
	 * @param vertices
	 *            the vertices to frame
	 * @param kind
	 *            the default annotated interface to frame the vertices as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by a vertex and interpreted
	 *         from the perspective of the annotate interface
	 */
	public <F> Iterable<F> frameVertices(final Iterable<Vertex> vertices,
			final Class<F> kind) {
		return new FramedVertexIterable<F>(this, vertices, kind);
	}

	/**
	 * A helper method for framing an iterable of edges.
	 * 
	 * @param edges
	 *            the edges to frame
	 * @param direction
	 *            the direction of the edges
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by an edge and interpreted
	 *         from the perspective of the annotate interface
	 *         
	 * @deprecated Use {@link #frameEdges(Iterable, Class)}, in combination with {@link InVertex} and {@link OutVertex}.
	 */
	public <F> Iterable<F> frameEdges(final Iterable<Edge> edges,
			final Direction direction, final Class<F> kind) {
		return new FramedEdgeIterable<F>(this, edges, direction, kind);
	}
	
	/**
	 * A helper method for framing an iterable of edges.
	 * 
	 * @param edges
	 *            the edges to frame
	 * @param direction
	 *            the direction of the edges
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by an edge and interpreted
	 *         from the perspective of the annotate interface
	 */
	public <F> Iterable<F> frameEdges(final Iterable<Edge> edges,
			final Class<F> kind) {
		return new FramedEdgeIterable<F>(this, edges, kind);
	}

	public Vertex getVertex(final Object id) {
		return config.getConfiguredGraph().getVertex(id);
	}

	/**
	 * Frame a vertex according to a particular kind of annotated interface.
	 * 
	 * @param id
	 *            the id of the vertex
	 * @param kind
	 *            the default annotated interface to frame the vertex as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy object backed by the vertex and interpreted from the
	 *         perspective of the annotate interface
	 */
	public <F> F getVertex(final Object id, final Class<F> kind) {
		return this.frame(getVertex(id), kind);
	}

	public Vertex addVertex(final Object id) {
		return config.getConfiguredGraph().addVertex(id);
	}

	/**
	 * Add a vertex to the underlying graph and return it as a framed vertex.
	 * 
	 * @param id
	 *            the id of the newly created vertex
	 * @param kind
	 *            the default annotated interface to frame the vertex as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy object backed by the vertex and interpreted from the
	 *         perspective of the annotate interface
	 */
	public <F> F addVertex(final Object id, final Class<F> kind) {
		Vertex vertex = addVertex(id);
		for (FrameInitializer initializer : config.getFrameInitializers()) {
			initializer.initElement(kind, this, vertex);
		}
		return this.frame(vertex, kind);
	}

	public Edge getEdge(final Object id) {
		return config.getConfiguredGraph().getEdge(id);
	}

	/**
	 * Frame an edge according to a particular kind of annotated interface.
	 * 
	 * @param id
	 *            the id of the edge
	 * @param direction
	 *            the direction of the edge
	 * @param kind
	 *            the default annotated interface to frame the edge as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy object backed by the edge and interpreted from the
	 *         perspective of the annotate interface
	 *         
	 * @deprecated Use {@link #getEdges(Object, Class)}, in combination with {@link InVertex} and {@link OutVertex}.      
	 */
	public <F> F getEdge(final Object id, final Direction direction,
			final Class<F> kind) {
		return this.frame(getEdge(id), direction, kind);
	}
	
	/**
	 * Frame an edge according to a particular kind of annotated interface.
	 * 
	 * @param id
	 *            the id of the edge
	 * @param direction
	 *            the direction of the edge
	 * @param kind
	 *            the default annotated interface to frame the edge as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy object backed by the edge and interpreted from the
	 *         perspective of the annotate interface     
	 */
	public <F> F getEdge(final Object id, final Class<F> kind) {
		return this.frame(getEdge(id), kind);
	}

	public Edge addEdge(final Object id, final Vertex outVertex,
			final Vertex inVertex, final String label) {
		return config.getConfiguredGraph().addEdge(id, outVertex, inVertex, label);
	}

	/**
	 * Add an edge to the underlying graph and return it as a framed edge.
	 * 
	 * @param id
	 *            the id of the newly created edge
	 * @param outVertex
	 *            the outgoing vertex
	 * @param inVertex
	 *            the incoming vertex
	 * @param label
	 *            the label of the edge
	 * @param direction
	 *            the direction of the edge
	 * @param kind
	 *            the default annotated interface to frame the edge as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return a proxy object backed by the edge and interpreted from the
	 *         perspective of the annotate interface
	 *         
	 * @deprecated Use {@link #addEdge(Object, Vertex, Vertex, String, Class)},
     *             in combination with {@link InVertex} and {@link OutVertex}.
	 */
	public <F> F addEdge(final Object id, final Vertex outVertex,
			final Vertex inVertex, final String label,
			final Direction direction, final Class<F> kind) {
		Edge edge = addEdge(id, outVertex, inVertex, label);
		for (FrameInitializer initializer : config.getFrameInitializers()) {
			initializer.initElement(kind, this, edge);
		}
		return this.frame(edge, direction, kind);
	}
	
	public <F> F addEdge(final Object id, final Vertex outVertex,
			final Vertex inVertex, final String label,
			final Class<F> kind) {
		return addEdge(id, outVertex, inVertex, label, Direction.OUT, kind);
	}

	public void removeVertex(final Vertex vertex) {
		config.getConfiguredGraph().removeVertex(vertex);
	}

	public void removeEdge(final Edge edge) {
		config.getConfiguredGraph().removeEdge(edge);
	}

	public Iterable<Vertex> getVertices() {
		return config.getConfiguredGraph().getVertices();
	}

	public Iterable<Vertex> getVertices(final String key, final Object value) {
		return config.getConfiguredGraph().getVertices(key, value);
	}

	/**
	 * Frame vertices according to a particular kind of annotated interface.
	 * 
	 * @param key
	 *            the key of the vertices to get
	 * @param value
	 *            the value of the vertices to get
	 * @param kind
	 *            the default annotated interface to frame the vertices as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by the vertices and
	 *         interpreted from the perspective of the annotate interface
	 */
	public <F> Iterable<F> getVertices(final String key, final Object value,
			final Class<F> kind) {
		return new FramedVertexIterable<F>(this, config.getConfiguredGraph().getVertices(
				key, value), kind);
	}

	public Iterable<Edge> getEdges() {
		return config.getConfiguredGraph().getEdges();
	}

	public Iterable<Edge> getEdges(final String key, final Object value) {
		return config.getConfiguredGraph().getEdges(key, value);
	}

	/**
	 * Frame edges according to a particular kind of annotated interface.
	 * 
	 * @param key
	 *            the key of the edges to get
	 * @param value
	 *            the value of the edges to get
	 * @param direction
	 *            the direction of the edges
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by the edges and interpreted
	 *         from the perspective of the annotate interface
	 *         
	 * @deprecated Use {@link #getEdges(String, Object, Class)}, in combination with
	 *             {@link InVertex} and {@link OutVertex}.
	 */
	public <F> Iterable<F> getEdges(final String key, final Object value,
			final Direction direction, final Class<F> kind) {
		return new FramedEdgeIterable<F>(this, config.getConfiguredGraph().getEdges(key,
				value), direction, kind);
	}
	
	/**
	 * Frame edges according to a particular kind of annotated interface.
	 * 
	 * @param key
	 *            the key of the edges to get
	 * @param value
	 *            the value of the edges to get
	 * @param direction
	 *            the direction of the edges
	 * @param kind
	 *            the default annotated interface to frame the edges as
	 * @param <F>
	 *            the default type of the annotated interface
	 * @return an iterable of proxy objects backed by the edges and interpreted
	 *         from the perspective of the annotate interface
	 */
	public <F> Iterable<F> getEdges(final String key, final Object value,
			final Class<F> kind) {
		return new FramedEdgeIterable<F>(this, config.getConfiguredGraph().getEdges(key,
				value), kind);
	}

	public Features getFeatures() {
		Features features = config.getConfiguredGraph().getFeatures().copyFeatures();
		features.isWrapper = true;
		return features;
	}

	public void shutdown() {
		config.getConfiguredGraph().shutdown();
	}

	public T getBaseGraph() {
		return this.baseGraph;
	}

	public String toString() {
		return StringFactory.graphString(this, this.baseGraph.toString());
	}

	/**
	 * The method used to register a new annotation handler for every new
	 * annotation a new annotation handler has to be registered in the framed
	 * graph
	 * 
	 * @param handler
	 *            the annotation handler
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public void registerAnnotationHandler(
			final AnnotationHandler<? extends Annotation> handler) {
		checkFactoryConfig();
		config.addAnnotationHandler(handler);
	}

	/**
	 * @param annotationType
	 *            the type of annotation handled by the annotation handler
	 * @return the annotation handler associated with the specified type
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public AnnotationHandler<?> getAnnotationHandler(
			final Class<? extends Annotation> annotationType) {
		checkFactoryConfig();
		return config.getAnnotationHandlers().get(annotationType);
	}


	/**
	 * @param annotationType
	 *            the type of annotation handled by the annotation handler
	 * @return a boolean indicating if the framedGraph has registered an
	 *         annotation handler for the specified type
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public boolean hasAnnotationHandler(
			final Class<? extends Annotation> annotationType) {
		checkFactoryConfig();
		return config.getAnnotationHandlers().containsKey(annotationType);
	}

	/**
	 * @param annotationType
	 *            the type of the annotation handler to remove
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public void unregisterAnnotationHandler(
			final Class<? extends Annotation> annotationType) {
		checkFactoryConfig();
		config.getAnnotationHandlers().remove(annotationType);
	}

	/**
	 * @return
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public Collection<AnnotationHandler<? extends Annotation>> getAnnotationHandlers() {
		checkFactoryConfig();
		return config.getAnnotationHandlers().values();
	}

	/**
	 * Register a <code>FrameInitializer</code> that will be called whenever a
	 * new vertex or edge is added to the graph. The initializer may mutate the
	 * vertex (or graph) before returning the framed element to the user.
	 * 
	 * @param frameInitializer
	 *            the frame initializer
	 * @deprecated Use {@link Module}s via {@link FramedGraphFactory}.
	 */
	public void registerFrameInitializer(FrameInitializer frameInitializer) {
		checkFactoryConfig();
		config.addFrameInitializer(frameInitializer);
	}
	
	private void checkFactoryConfig() {
		if(configViaFactory) {
			throw new UnsupportedOperationException("Unsupported for FramedGraph configured by factory");
		}
	}

	
	FramedGraphConfiguration getConfig() {
		return config;
	}

	
	 /**
     * Generate a query object that can be used to fine tune which edges/vertices are retrieved from the graph.
     *
     * @return a graph query object with methods for constraining which data is pulled from the underlying graph
     */
    public FramedGraphQuery query() {
    	return new FramedGraphQueryImpl(this, config.getConfiguredGraph().query());
    }
}
