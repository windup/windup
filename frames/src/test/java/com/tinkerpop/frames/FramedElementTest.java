package com.tinkerpop.frames;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;
import com.tinkerpop.frames.domain.incidences.Created;
import com.tinkerpop.frames.domain.incidences.CreatedBy;
import com.tinkerpop.frames.domain.incidences.CreatedInfo;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedElementTest {

	@Test
    public void testGettingProperties() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals(marko.getName(), "marko");
        assertEquals(marko.getAge(), new Integer(29));

        Project lop = framedGraph.getVertex(3, Project.class);
        assertEquals(lop.getName(), "lop");
        assertEquals(lop.getLanguage(), "java");

        CreatedInfo markoCreatedLopInfo = framedGraph.getEdge(9, CreatedInfo.class);
        assertEquals(markoCreatedLopInfo.getWeight(), 0.4f, 0.1f);
        //Same with using deprecated Domain/Range annotations:
        Created markoCreatedLop = framedGraph.getEdge(9, Direction.OUT, Created.class);
        assertEquals(markoCreatedLop.getWeight(), 0.4f, 0.1f);
        CreatedBy lopCreatedByMarko = framedGraph.getEdge(9, Direction.IN, CreatedBy.class);
        assertEquals(lopCreatedByMarko.getWeight(), 0.4f, 0.1f);

        Person temp = framedGraph.frame(graph.addVertex(null), Person.class);
        assertNull(temp.getName());
        assertNull(temp.getAge());

    }

	@Test
    public void testSettingProperties() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals(marko.getName(), "marko");
        marko.setName("pavel");
        assertEquals(marko.getName(), "pavel");
        assertEquals(marko.getAge(), new Integer(29));
        marko.setAge(31);
        assertEquals(marko.getAge(), new Integer(31));
        
        CreatedInfo markoCreatedLopInfo = framedGraph.getEdge(9, CreatedInfo.class);
        assertEquals(markoCreatedLopInfo.getWeight(), 0.4f, 0.1f);
        markoCreatedLopInfo.setWeight(99.0f);
        assertEquals(markoCreatedLopInfo.getWeight(), 99.0f, 0.1f);
        markoCreatedLopInfo.setWeight(0.4f);
        
        //Same with deprecated Domain/Range annotations:
        Created markoCreatedLop = framedGraph.getEdge(9, Direction.OUT, Created.class);
        assertEquals(markoCreatedLop.getWeight(), 0.4f, 0.1f);
        markoCreatedLop.setWeight(99.0f);
        assertEquals(markoCreatedLop.getWeight(), 99.0f, 0.1f);
    }

    @Test
    public void testRemoveProperties() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals(marko.getAge(), new Integer(29));
        marko.removeAge();
        assertNull(marko.getAge());
    }

    @Test
    public void testSetPropertiesToNull() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals(marko.getAge(), new Integer(29));
        marko.setAge(null);
        assertNull(marko.getAge());
    }

    @Test
    public void testEnumProperty() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals(marko.getGender(), null);
        marko.setGender(Person.Gender.MALE);
        assertEquals(Person.Gender.MALE, marko.getGender());
        marko.setGender(null);
        assertEquals(null, marko.getGender());
        marko.setGender(Person.Gender.MALE);
        marko.removeGender();
        assertEquals(marko.getGender(), null);
    }

    @Test
    public void testToString() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);
        assertEquals("v[1]", marko.toString());

        CreatedInfo markoCreatedLopInfo = framedGraph.getEdge(9, CreatedInfo.class);
        assertEquals("e[9][1-created->3]", markoCreatedLopInfo.toString());
        //Using deprecated Domain/Range annotations:
        Created markoCreatedLop = framedGraph.getEdge(9, Direction.OUT, Created.class);
        assertEquals("e[9][1-created->3]", markoCreatedLop.toString());
    }

    @Test
    public void testEquality() {
        TinkerGraph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<TinkerGraph> framedGraph = new FramedGraphFactory().create(graph);

        assertEquals(framedGraph.getVertex(1, Person.class), framedGraph.frame(graph.getVertex(1), Person.class));

    }
    
    @Test(expected=UnhandledMethodException.class)
    public void testUnhandledMethodNoAnnotation() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);    	
        marko.unhandledNoAnnotation();
    }
    
    @Test(expected=UnhandledMethodException.class)
    public void testUnhandledMethodWithAnnotation() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraphFactory().create(graph);

        Person marko = framedGraph.getVertex(1, Person.class);    	
        marko.unhandledNoHandler();
    }
}
