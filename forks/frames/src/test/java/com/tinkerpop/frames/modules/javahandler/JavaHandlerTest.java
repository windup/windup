package com.tinkerpop.frames.modules.javahandler;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;
import com.tinkerpop.frames.domain.incidences.Knows;

/**
 * @author Bryn Cooke
 *
 */
public class JavaHandlerTest {

	private FramedGraph<TinkerGraph> g;

	@Test
	public void testJavaHandlerVertices() {
		
		Person person = g.getVertex(1, Person.class);
		String profile = person.getNameAndAge();
		Assert.assertEquals("marko (29)", profile);

		Person person2 = g.getVertex(2, Person.class);
		String profile2 = person2.getNameAndAge();
		Assert.assertEquals("vadas (27)", profile2);

		Set<Person> coCreators = new HashSet<Person>();

		Iterables.addAll(coCreators, person.getCoCreatorsJava());
		Assert.assertEquals(Sets.newHashSet(g.getVertex(4), g.getVertex(6)), coCreators);

	}

	@Test
	public void testJavaHandlerEdges() {
		Knows knows = g.getEdge(7, Direction.OUT, Knows.class);
		Assert.assertEquals("marko<->vadas", knows.getNames());
	}
	
	
	@Test
	public void testJavaHandlerClassAnnotation() {
		Project project = g.getVertex(5, Project.class);
		Assert.assertEquals("java", project.getLanguageUsingMixin());
	}
	
	@Test(expected=JavaHandlerException.class)
	public void testMethodNotImplemented() {
		Person person = g.getVertex(1, Person.class);
		person.notImplemented();
	}
	
	@Test
	public void testIntitializersVertices() {
		A a = g.addVertex(null, A.class);
		Assert.assertEquals("A", a.getProperty());
		B b = g.addVertex(null, B.class);
		Assert.assertEquals("AB", b.getProperty());
		C c = g.addVertex(null, C.class);
        Assert.assertTrue(c.getProperty().equals("ABCC2") || c.getProperty().equals("ABC2C"));
	}
	
	@Test
	public void testIntitializersEdges() {
		Vertex v = g.addVertex(null);
		
		A a = g.addEdge(null, v, v, "test", A.class);
		Assert.assertEquals("A", a.getProperty());
		B b = g.addEdge(null, v, v, "test", B.class);
		Assert.assertEquals("AB", b.getProperty());
		C c = g.addEdge(null, v, v, "test", C.class);
		Assert.assertTrue(c.getProperty().equals("ABCC2") || c.getProperty().equals("ABC2C"));
	}
	
	interface A {
		
		@Property("property")
		String getProperty();
		
		@Property("property")
		void setProperty(String property);
		
		abstract class Impl implements A {
			@Initializer
			void init() {
				setProperty("A");
			}
		}
	}
	
	interface B extends A {
		abstract class Impl implements B {
			@Initializer
			void init() {
				setProperty(getProperty() + "B");
			}
		}
	}
	
	
	interface C extends A, B {
		abstract class Impl implements C {
			@Initializer
			void init() {
				setProperty(getProperty() + "C");
			}
			
			@Initializer
			void init2() {
				setProperty(getProperty() + "C2");
			}
		}
	}

	@Before
	public void setup() {
		TinkerGraph base = TinkerGraphFactory.createTinkerGraph();
		g = new FramedGraphFactory(new JavaHandlerModule()).create(base);
		
	}
	
	
}
