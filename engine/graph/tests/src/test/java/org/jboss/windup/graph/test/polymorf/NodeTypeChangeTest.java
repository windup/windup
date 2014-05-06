package org.jboss.windup.graph.test.polymorf;

import java.io.File;

import org.jboss.forge.furnace.impl.util.Files;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
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

    @Test
    public void testNodeTypeChange()
    {
        File graphDir = OperatingSystemUtils.getTempDirectory();
        try
        {
            Graph g = GraphCreator.createFamilyGraph(graphDir);

            Module tgmp = new TypedGraphModuleBuilder()
                        .withClass(SpecialPerson.class).build();

            FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule(), tgmp);
            FramedGraph<Graph> framed = factory.create(g);

            final Vertex v = g.getVertices().iterator().next();

            // Should be Person.
            Person foo = framed.frame(v, Person.class);
            System.out.println("Person: " + foo + " " + asString(foo));

            // Try to retype
            v.setProperty("type", "special");

            // Should be SpecialPerson.
            SpecialPerson foo2 = (SpecialPerson) framed.frame(v, Person.class);
            System.out.println("SpecialPerson: " + foo2 + " " + asString(foo2));

            // Unknown type - should be Person.
            v.setProperty("type", "aaaaaa");

            foo = framed.frame(v, Person.class);
            System.out.println("Not SpecialPerson: " + foo + " " + asString(foo));
        }
        finally
        {
            Files.delete(graphDir, true);
        }

    }

    private static String asString(Person p)
    {
        if (p == null)
            return "(null)";
        return "Person name: " + p.getName() + " " + p.getClass().getSimpleName();
    }

}// class
