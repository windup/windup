package com.tinkerpop.frames.structures;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.domain.classes.Person;
import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedVertexSetTest extends TestCase {

    public void testFramedSet() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        FramedGraph<Graph> framedGraph = new FramedGraph<Graph>(graph);
        Set<Vertex> vertices = new HashSet<Vertex>();
        vertices.add(graph.getVertex(1));
        vertices.add(graph.getVertex(4));
        vertices.add(graph.getVertex(6));
        FramedVertexSet<Person> set = new FramedVertexSet<Person>(framedGraph, vertices, Person.class);
        assertEquals(set.size(), 3);
        assertTrue(set.contains(graph.getVertex(1)));
        assertTrue(set.contains(graph.getVertex(4)));
        assertTrue(set.contains(graph.getVertex(6)));
        assertTrue(set.contains(framedGraph.frame(graph.getVertex(1), Person.class)));
        assertTrue(set.contains(framedGraph.frame(graph.getVertex(4), Person.class)));
        assertTrue(set.contains(framedGraph.frame(graph.getVertex(6), Person.class)));

        int counter = 0;
        for (Person person : set) {
            assertTrue(person.asVertex().getId().equals("1") || person.asVertex().getId().equals("4") || person.asVertex().getId().equals("6"));
            counter++;
        }
        assertEquals(counter, 3);
    }
}
