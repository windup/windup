package org.jboss.windup.graph.test.polymorf;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class NodeTypeChangeTest {
    private static final Logger log = LoggerFactory.getLogger( NodeTypeChangeTest.class );
    
    @Test
    public void testNodeTypeChange() {
        Graph g = GraphCreator.createFamilyGraph();
        
        Module tgmp = new TypedGraphModuleBuilder()
                .withClass(SpecialPerson.class) .build();
        
        FramedGraphFactory factory = new FramedGraphFactory( new GremlinGroovyModule(), tgmp );
        FramedGraph framed = factory.create(g);
        
        final Vertex v = g.getVertices().iterator().next();
        
        // Should be Person.
        Person foo = (Person) framed.frame( v, Person.class );
        System.out.println( "Person: " + foo + " " + asString( foo ) );
        
        // Try to retype
        v.setProperty("type", "special");
        
        // Should be SpecialPerson.
        SpecialPerson foo2 = (SpecialPerson) framed.frame( v, Person.class );
        System.out.println( "SpecialPerson: " + foo2 + " " + asString( foo2 ) );

        // Unknown type - should be Person.
        v.setProperty("type", "aaaaaa");
        
        foo = (Person) framed.frame( v, Person.class );
        System.out.println( "Not SpecialPerson: " + foo + " " + asString( foo ) );
    }
    
    
    private static String asString( Person p ){
        if( p == null ) return "(null)";
        return "Person name: " + p.getName() + " " + p.getClass().getSimpleName();
    }

}// class
