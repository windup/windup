package org.jboss.windup.graph.test.polymorf;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class NodeTypeChangeTest
{
    private Graph g;

    @Before
    public void setUpEnv()
    {
        this.g = GraphCreator.createFamilyGraph();
    }

    @After
    public void tearDown()
    {
        this.g.shutdown();
    }

    @Test
    public void testNodeTypeChange()
    {
        Module tgmp = new TypedGraphModuleBuilder()
                    .withClass(SpecialPerson.class).build();

        FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule(), tgmp);
        FramedGraph<Graph> framed = factory.create(g);

        final Vertex v = g.getVertices().iterator().next();

        // Should be Person.
        Person foo = framed.frame(v, Person.class);
        System.out.println("Person: " + foo + " " + asString(foo));

        // Try to retype
        v.setProperty(WindupVertexFrame.TYPE_PROP, "special");

        // Should be SpecialPerson.
        SpecialPerson foo2 = (SpecialPerson) framed.frame(v, Person.class);
        System.out.println("SpecialPerson: " + foo2 + " " + asString(foo2));

        // Unknown type - should be Person.
        v.setProperty(WindupVertexFrame.TYPE_PROP, "aaaaaa");

        foo = framed.frame(v, Person.class);
        System.out.println("Not SpecialPerson: " + foo + " " + asString(foo));
    }

    private static String asString(Person p)
    {
        if (p == null)
            return "(null)";
        return "Person name: " + p.getName() + " " + p.getClass().getSimpleName();
    }

}