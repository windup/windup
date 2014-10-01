package com.tinkerpop.frames.modules;

import org.junit.Assert;
import org.junit.Test;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.incidences.Knows;
import com.tinkerpop.frames.modules.AbstractModule;
import com.tinkerpop.frames.modules.TypeResolver;

/**
 * @author Bryn Cooke
 */
public class TypeResolverTest {

	private FramedGraph<Graph> framedGraph;

	public static interface AdditionalVertex {

	}

	public static interface ExtendedPerson extends Person {

	}

	public static interface AdditionalEdge {

	}

	@Test
	public void testAdditionalTypes() {
		Graph graph = TinkerGraphFactory.createTinkerGraph();
		FramedGraphFactory factory = new FramedGraphFactory(new AbstractModule() {

			@Override
			public void doConfigure(FramedGraphConfiguration config) {
				config.addTypeResolver(new TypeResolver() {

					@Override
					public Class<?>[] resolveTypes(Edge e, Class<?> defaultType) {

						return new Class[] { AdditionalEdge.class };
					}

					@Override
					public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType) {
						return new Class[] { AdditionalVertex.class };
					}
				});
			}
		});
		framedGraph = factory.create(graph);

		Person marko = framedGraph.getVertex(1, Person.class);
		Assert.assertTrue(marko instanceof AdditionalVertex);
		Assert.assertFalse(marko instanceof AdditionalEdge);

		Knows knows = marko.getKnows().iterator().next();
		Assert.assertTrue(knows instanceof AdditionalEdge);
		Assert.assertFalse(knows instanceof AdditionalVertex);

	}

	@Test
	public void testExtendedTypes() {
		Graph graph = TinkerGraphFactory.createTinkerGraph();
		FramedGraphFactory factory = new FramedGraphFactory(new AbstractModule() {

			@Override
			public void doConfigure(FramedGraphConfiguration config) {
				config.addTypeResolver(new TypeResolver() {

					@Override
					public Class<?>[] resolveTypes(Edge e, Class<?> defaultType) {

						return new Class[0];
					}

					@Override
					public Class<?>[] resolveTypes(Vertex v, Class<?> defaultType) {
						return new Class[] { ExtendedPerson.class };
					}
				});
			}

		});
		framedGraph = factory.create(graph);

		Person marko = framedGraph.getVertex(1, Person.class);
		Assert.assertTrue(marko instanceof ExtendedPerson);

	}

}
