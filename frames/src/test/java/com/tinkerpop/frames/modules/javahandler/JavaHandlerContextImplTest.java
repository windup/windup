package com.tinkerpop.frames.modules.javahandler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;

public class JavaHandlerContextImplTest {

	private FramedGraph<?> graph;
	private Vertex vertex;
	private Edge edge;

	@Before
	public void setup() {
		graph = Mockito.mock(FramedGraph.class);
		vertex = Mockito.mock(Vertex.class);
		edge = Mockito.mock(Edge.class);
	}
	
	@Test
	public void testGetGraph() throws NoSuchMethodException {
		Assert.assertEquals(graph, getHandler(graph, null, null).g());
	}
	
	@Test
	public void testGetContext() throws NoSuchMethodException {
		Assert.assertEquals(vertex, getHandler(graph, vertex, null).it());
	}
	
	@Test
	public void testFrameVertexDefault() throws NoSuchMethodException {
		
		getHandler(graph, vertex, getMethod("getA")).frame(vertex);
		Mockito.verify(graph).frame(vertex, A.class);
	}
	
	@Test
	public void testFrameVertexExplicit() throws NoSuchMethodException {
		getHandler(graph, vertex, getMethod("getA")).frame(vertex, B.class);
		Mockito.verify(graph).frame(vertex, B.class);
	}
	
		
	@Test
	public void testFrameEdgeDefault() throws NoSuchMethodException {
		getHandler(graph, edge, getMethod("getA")).frame(edge, Direction.OUT);
		Mockito.verify(graph).frame(edge, Direction.OUT, A.class);
	}

	@Test
	public void testFrameEdgeDefaultExplicit() throws NoSuchMethodException {
		getHandler(graph, edge, getMethod("getA")).frame(edge, Direction.OUT, B.class);
		Mockito.verify(graph).frame(edge, Direction.OUT, B.class);
	}
	
	@Test
	public void testFrameEdgeDefaultNoDirection() throws NoSuchMethodException {
		getHandler(graph, edge, getMethod("getA")).frame(edge);
		Mockito.verify(graph).frame(edge, A.class);
	}

	@Test
	public void testFrameEdgeDefaultExplicitNoDirection() throws NoSuchMethodException {
		getHandler(graph, edge, getMethod("getA")).frame(edge, B.class);
		Mockito.verify(graph).frame(edge, B.class);
	}

	
	@Test(expected=JavaHandlerException.class)
	public void testFrameNotIterable() throws NoSuchMethodException {
		
		getHandler(graph, vertex, getMethod("getA")).frameVertices(Lists.newArrayList(vertex));
		Mockito.verify(graph).frame(vertex, A.class);
	}

	@Test(expected=JavaHandlerException.class)
	public void testFrameIterableNotGeneric() throws NoSuchMethodException {
		getHandler(graph, vertex, getMethod("getUnknownIterable")).frameVertices(Lists.newArrayList(vertex));
		Mockito.verify(graph).frame(vertex, A.class);
	}
	
	@Test
	public void testFrameIterableVertex() throws NoSuchMethodException {
		ArrayList<Vertex> iterable = Lists.newArrayList(vertex);
		getHandler(graph, vertex, getMethod("getIterable")).frameVertices(iterable);
		Mockito.verify(graph).frameVertices(iterable, A.class);
	}
	
	@Test
	public void testFrameIterableVertexComplex() throws NoSuchMethodException {
		ArrayList<Vertex> iterable = Lists.newArrayList(vertex);
		getHandler(graph, vertex, getMethod("getComplexIterable")).frameVertices(iterable);
		Mockito.verify(graph).frameVertices(iterable, A.class);
	}
	
	@Test
	public void testFrameIterableVertexExplicit() throws NoSuchMethodException {
		ArrayList<Vertex> iterable = Lists.newArrayList(vertex);
		getHandler(graph, vertex, getMethod("getIterable")).frameVertices(iterable, B.class);
		Mockito.verify(graph).frameVertices(iterable, B.class);
	}
	
	@Test
	public void testFrameIterableEdge() throws NoSuchMethodException {
		ArrayList<Edge> iterable = Lists.newArrayList(edge);
		getHandler(graph, vertex, getMethod("getIterable")).frameEdges(iterable, Direction.OUT);
		Mockito.verify(graph).frameEdges(iterable, Direction.OUT, A.class);
	}
	
	@Test
	public void testFrameIterableEdgeExplicit() throws NoSuchMethodException {
		ArrayList<Edge> iterable = Lists.newArrayList(edge);
		getHandler(graph, vertex, getMethod("getIterable")).frameEdges(iterable, Direction.OUT, B.class);
		Mockito.verify(graph).frameEdges(iterable, Direction.OUT, B.class);
	}
	
	@Test
	public void testFrameIterableEdgeNoDirection() throws NoSuchMethodException {
		ArrayList<Edge> iterable = Lists.newArrayList(edge);
		getHandler(graph, vertex, getMethod("getIterable")).frameEdges(iterable);
		Mockito.verify(graph).frameEdges(iterable, A.class);
	}
	
	@Test
	public void testFrameIterableEdgeExplicitNoDirection() throws NoSuchMethodException {
		ArrayList<Edge> iterable = Lists.newArrayList(edge);
		getHandler(graph, vertex, getMethod("getIterable")).frameEdges(iterable, B.class);
		Mockito.verify(graph).frameEdges(iterable, B.class);
	}
	
	@Test
	public void testGremlinContext() throws NoSuchMethodException {
		JavaHandlerContextImpl<Edge> handler = getHandler(graph, edge, getMethod("getA"));
		Assert.assertEquals(edge, handler.gremlin().next());
		
	}

	@Test
	public void testGremlinExplicit() throws NoSuchMethodException {
		JavaHandlerContextImpl<Edge> handler = getHandler(graph, edge, getMethod("getA"));
		Assert.assertEquals(vertex, handler.gremlin(vertex).next());
		
	}

	private Method getMethod(String name) throws NoSuchMethodException {
		return JavaHandlerContextImplTest.class.getMethod(name);
	}
	
	

	private <T extends Element> JavaHandlerContextImpl<T> getHandler(FramedGraph<?> graph, T element, Method method) {
		JavaHandlerContextImpl<T> impl = new JavaHandlerContextImpl<T>(graph, method, element);
		return impl;
	}
	
	public A getA() {
		return null;
	}
	
	public Iterable getUnknownIterable() {
		return null;
	}
	
	public Iterable<A> getIterable() {
		return null;
	}
	
	public static interface A {
		
	}

	public static interface B {
		
	}

	public <T extends A> Iterable<T> getComplexIterable() {
		return null;
	}
}
