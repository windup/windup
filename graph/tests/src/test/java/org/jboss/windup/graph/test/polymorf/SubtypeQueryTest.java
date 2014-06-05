package org.jboss.windup.graph.test.polymorf;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.modules.Module;
import com.tinkerpop.frames.modules.gremlingroovy.GremlinGroovyModule;
import com.tinkerpop.frames.modules.typedgraph.TypedGraphModuleBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class SubtypeQueryTest {
    private static final Logger log = LoggerFactory.getLogger( SubtypeQueryTest.class );

    private Graph g;
    
    @Before
    public void setUpEnv(){
        this.g = GraphCreator.createFamilyGraph();
    }
    
    @After
    public void tearDown(){
        this.g.shutdown();
    }
    
    
    @Test
    public void testSubtypeQuery()
    {
        Module tgmp = new TypedGraphModuleBuilder()
                    .withClass(SpecialPerson.class).build();

        FramedGraphFactory factory = new FramedGraphFactory(new GremlinGroovyModule(), tgmp);
        FramedGraph<Graph> framed = factory.create(g);

        // Huh, there's no query for all vertices of given type 8-o 
        //framed.
               
    }
    

}// class
