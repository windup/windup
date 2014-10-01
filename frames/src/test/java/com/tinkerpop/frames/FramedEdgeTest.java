package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraphFactory;
import com.tinkerpop.frames.domain.classes.Person;
import com.tinkerpop.frames.domain.classes.Project;
import com.tinkerpop.frames.domain.incidences.Created;
import com.tinkerpop.frames.domain.incidences.CreatedBy;
import com.tinkerpop.frames.domain.incidences.CreatedInfo;
import com.tinkerpop.frames.domain.incidences.Knows;
import com.tinkerpop.frames.domain.incidences.WeightedEdge;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class FramedEdgeTest {

    private FramedGraph<Graph> framedGraph;
    private Person marko;
    private Person vadas;
    private Person peter;
    private Person josh;
    private Knows knows;
    private Project lop;

    @Before
    public void setup() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        framedGraph = new FramedGraphFactory().create(graph);

        marko = framedGraph.getVertex(1, Person.class);
        vadas = framedGraph.getVertex(2, Person.class);
        peter = framedGraph.getVertex(6, Person.class);
        josh = framedGraph.getVertex(4, Person.class);
        knows = framedGraph.getEdge(7, Knows.class);
        lop = framedGraph.getVertex(3, Project.class);
    }

    @Test
    public void testGettingOutAndIn() {

        assertEquals(marko, knows.getOut());
        assertEquals(vadas, knows.getIn());



        CreatedInfo created = lop.getCreatedInfo().iterator().next();
        assertEquals(lop, created.getProject());
        assertTrue(created.getPerson().equals(marko) || created.getPerson().equals(peter) || created.getPerson().equals(josh));
        
        created = marko.getCreatedInfo().iterator().next();
        assertEquals(lop, created.getProject());
        assertEquals(marko, created.getPerson());
    }

    @Test
    public void testGettingDomainAndRange() {

        assertEquals(marko, knows.getDomain());
        assertEquals(vadas, knows.getRange());

        CreatedBy createdBy = lop.getCreatedBy().iterator().next();
        assertEquals(lop, createdBy.getDomain());
        assertTrue(createdBy.getRange().equals(marko) || createdBy.getRange().equals(peter) || createdBy.getRange().equals(josh));
        
        Created created = marko.getCreated().iterator().next();
        //Please note: the below results are actually incorrect: the domain and range are incorrectly tagged
        // in Created for usage with @Incidence. I'm not going to fix that in the test-cases as Domain and
        // Range are deprecated now. The incorrect annotations probable show better than anything that
        // the now deprecated annotations are quite confusing:
        assertEquals(lop, created.getRange()); //range actually returns a Person, not a Project...
        assertEquals(marko, created.getDomain()); //domain actually returns a Project, not a Person...
        



    }

    

    
    /**
     * Uses deprecated Domain/range annotations
     */
    @Test
    public void testGettingIterableDeprecated() {

        Iterator<Edge> edges = framedGraph.getEdges("weight", 0.4f).iterator();
        Iterator<Created> createds = framedGraph.getEdges("weight", 0.4f, Direction.OUT, Created.class).iterator();

        int counter = 0;
        while (edges.hasNext()) {
            assertEquals(edges.next(), createds.next().asEdge());
            counter++;
        }
        assertEquals(counter, 2);
        assertFalse(edges.hasNext());
        assertFalse(createds.hasNext());

    }

    @Test
    public void testGettingIterable() {

        Iterator<Edge> edges = framedGraph.getEdges("weight", 0.4f).iterator();
        Iterator<CreatedInfo> createds = framedGraph.getEdges("weight", 0.4f, CreatedInfo.class).iterator();

        int counter = 0;
        while (edges.hasNext()) {
            assertEquals(edges.next(), createds.next().asEdge());
            counter++;
        }
        assertEquals(counter, 2);
        assertFalse(edges.hasNext());
        assertFalse(createds.hasNext());

    }

    /**
     * Uses deprecated Domain/Range annotations
     */
    @Test
    public void testEqualityOfIterableMethodsDeprecated() {

        Iterator<Created> createds1 = framedGraph.frameEdges(framedGraph.getEdges("weight", 0.4f), Direction.OUT, Created.class).iterator();
        Iterator<Created> createds2 = framedGraph.getEdges("weight", 0.4f, Direction.OUT, Created.class).iterator();

        int counter = 0;
        while (createds1.hasNext()) {
            assertEquals(createds1.next(), createds2.next());
            counter++;
        }
        assertEquals(counter, 2);
        assertFalse(createds1.hasNext());
        assertFalse(createds2.hasNext());

    }

    @Test
    public void testEqualityOfIterableMethods() {

        Iterator<CreatedInfo> createds1 = framedGraph.frameEdges(framedGraph.getEdges("weight", 0.4f), CreatedInfo.class).iterator();
        Iterator<CreatedInfo> createds2 = framedGraph.getEdges("weight", 0.4f, CreatedInfo.class).iterator();

        int counter = 0;
        while (createds1.hasNext()) {
            assertEquals(createds1.next(), createds2.next());
            counter++;
        }
        assertEquals(counter, 2);
        assertFalse(createds1.hasNext());
        assertFalse(createds2.hasNext());

    }

    @Test
    public void testEquality() {

        //Deprecated Domain/Range:
        Created created = marko.getCreated().iterator().next();
        WeightedEdge weightedEdge = framedGraph.frame(created.asEdge(), Direction.OUT, WeightedEdge.class);
        assertEquals(created, weightedEdge);
        
        //Initial/Terminal:
        CreatedInfo createdInfo = marko.getCreatedInfo().iterator().next();
        assertEquals(createdInfo, weightedEdge);
    }
}
