package com.tinkerpop.frames;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.junit.Test;

import junit.framework.Assert;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.EdgeTestSuite;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphTestSuite;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.VertexTestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.blueprints.util.io.gml.GMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLReaderTestSuite;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONReaderTestSuite;
import com.tinkerpop.frames.annotations.AdjacencyAnnotationHandler;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.annotations.DomainAnnotationHandler;
import com.tinkerpop.frames.annotations.InVertexAnnotationHandler;
import com.tinkerpop.frames.annotations.IncidenceAnnotationHandler;
import com.tinkerpop.frames.annotations.OutVertexAnnotationHandler;
import com.tinkerpop.frames.annotations.PropertyMethodHandler;
import com.tinkerpop.frames.annotations.RangeAnnotationHandler;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovyAnnotationHandler;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;
import com.tinkerpop.frames.domain.incidences.Knows;
import com.tinkerpop.frames.modules.MethodHandler;


/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedGraphTest extends GraphTest {

    public void testDeprecatedAnnotationUnregister() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraph<Graph>(graph);

        int counter = framedGraph.getAnnotationHandlers().size();
        for (AnnotationHandler a : new HashSet<AnnotationHandler>(framedGraph.getAnnotationHandlers())) {
            assertTrue(framedGraph.hasAnnotationHandler(a.getAnnotationType()));
            counter--;
            framedGraph.unregisterAnnotationHandler(a.getAnnotationType());
            assertEquals(framedGraph.getAnnotationHandlers().size(), counter);

        }
        assertEquals(framedGraph.getAnnotationHandlers().size(), 0);
    }
    
    
	public void testDeprecatedConfigContainsCoreAnnotationHandlers() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraph<Graph>(graph);
        Collection<Class<?>> collections = Collections2.transform(framedGraph.getAnnotationHandlers(), new Function<AnnotationHandler<? extends Annotation>, Class<?>>() {
			@Override
			public Class<?> apply(AnnotationHandler<? extends Annotation> handler) {
				return handler.getClass();
			}
        });
        Assert.assertTrue(collections.containsAll(Arrays.asList( 
        		AdjacencyAnnotationHandler.class, 
        		IncidenceAnnotationHandler.class, 
        		DomainAnnotationHandler.class,
        		RangeAnnotationHandler.class,
        		InVertexAnnotationHandler.class,
        		OutVertexAnnotationHandler.class,
        		GremlinGroovyAnnotationHandler.class)));
    }
	
	public void testDeprecatedConfigRegisterAnnotationHandlers() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraph<Graph>(graph);
        framedGraph.getAnnotationHandlers().clear();
        MethodHandler<?> handler = new PropertyMethodHandler();
        framedGraph.getConfig().addMethodHandler(handler);
        Assert.assertEquals(1, framedGraph.getConfig().getMethodHandlers().size());
        Assert.assertTrue(framedGraph.getConfig().getMethodHandlers().containsValue(handler));
        
    }


    public void testFrameEquality() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        assertEquals(framedGraph.frame(graph.getVertex(1), Person.class), framedGraph.getVertex(1, Person.class));
        assertEquals(framedGraph.frame(graph.getEdge(7), Knows.class), framedGraph.getEdge(7, Knows.class));
    }

    public void testFrameVertices() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        int counter = 0;
        for (Person person : framedGraph.getVertices("name", "marko", Person.class)) {
            counter++;
            assertEquals(person.getName(), "marko");
        }
        assertEquals(counter, 1);

        counter = 0;
        for (Project project : framedGraph.frameVertices(graph.getVertices("lang", "java"), Project.class)) {
            counter++;
            assertTrue(project.getName().equals("lop") || project.getName().equals("ripple"));
        }
        assertEquals(counter, 2);

    }

    public void testCreateFrame() {
        Graph graph = new TinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);
        Person person = framedGraph.addVertex(null, Person.class);
        assertEquals(person.asVertex(), graph.getVertices().iterator().next());
        int counter = 0;
        for (Vertex v : graph.getVertices()) {
            counter++;
        }
        assertEquals(counter, 1);
        counter = 0;
        for (Edge e : graph.getEdges()) {
            counter++;
        }
        assertEquals(counter, 0);
        Person person2 = framedGraph.addVertex("aPerson", Person.class);
        assertEquals(person2.asVertex().getId(), "aPerson");
        counter = 0;
        for (Vertex v : graph.getVertices()) {
            counter++;
        }
        assertEquals(counter, 2);


    }

    public void testCreateFrameForNonexistantElements() {
        Graph graph = new TinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);
        Person vertex = framedGraph.getVertex(-1, Person.class);
        Assert.assertNull(vertex);
        vertex = framedGraph.frame((Vertex)null, Person.class);
        Assert.assertNull(vertex);
        
        Knows edge = framedGraph.getEdge(-1, Knows.class);
        Assert.assertNull(edge);
        edge = framedGraph.frame((Edge)null, Knows.class);
        Assert.assertNull(edge);
        
    }
    

    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }

    public void testGraphMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphMLReaderTestSuite(this));
        printTestPerformance("GraphMLReaderTestSuite", this.stopWatch());
    }

    public void testGMLReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GMLReaderTestSuite(this));
        printTestPerformance("GMLReaderTestSuite", this.stopWatch());
    }

    public void testGraphSONReaderTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphSONReaderTestSuite(this));
        printTestPerformance("GraphSONReaderTestSuite", this.stopWatch());
    }

    public Graph generateGraph() {
        final TinkerGraph baseGraph = new TinkerGraph();
        baseGraph.getFeatures().isPersistent = false;
        return new FramedGraph<TinkerGraph>(baseGraph);
    }

    public Graph generateGraph(final String directory) {
        return this.generateGraph();
    }

    public void doTestSuite(final TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                method.invoke(testSuite);
            }
        }
    }
    
    @Test
	public void testVertexQuery() {
		Graph graph = TinkerGraphFactory.createTinkerGraph();
		FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);
		Assert.assertEquals(6, Iterables.size(framedGraph.query().has("name").vertices(VertexFrame.class)));
		Iterables.elementsEqual(
				framedGraph.query().has("name").vertices(),
				unwrapVertices(framedGraph.query().has("name")
						.vertices(VertexFrame.class)));
		
		Assert.assertEquals(1, Iterables.size(framedGraph.query().has("name").limit(1).vertices(VertexFrame.class)));
		Iterables.elementsEqual(
				framedGraph.query().has("name").limit(1).vertices(),
				unwrapVertices(framedGraph.query().has("name").limit(1)
						.vertices(VertexFrame.class)));

	}

	
	@Test
	public void testEdgeQuery() {
		Graph graph = TinkerGraphFactory.createTinkerGraph();
		
		FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);
		
		Assert.assertEquals(6, Iterables.size(framedGraph.query().has("weight").edges(EdgeFrame.class)));
		Iterables.elementsEqual(
				framedGraph.query().has("weight").edges(),
				unwrapEdges(framedGraph.query().has("weight")
						.edges(EdgeFrame.class)));
		
		Assert.assertEquals(1, Iterables.size(framedGraph.query().has("weight").limit(1).edges(EdgeFrame.class)));
		Iterables.elementsEqual(
				framedGraph.query().has("weight").limit(1).edges(),
				unwrapEdges(framedGraph.query().has("weight").limit(1)
						.edges(EdgeFrame.class)));

	}





	
	private Iterable<Vertex> unwrapVertices(Iterable<VertexFrame> it) {
		return Iterables.transform(it, new Function<VertexFrame, Vertex>() {
			@Override
			public Vertex apply(VertexFrame input) {
				return input.asVertex();
			}
		});
	}
	
	private Iterable<Edge> unwrapEdges(Iterable<EdgeFrame> it) {
		return Iterables.transform(it, new Function<EdgeFrame, Edge>() {
			@Override
			public Edge apply(EdgeFrame input) {
				return input.asEdge();
			}
		});
	}


}
