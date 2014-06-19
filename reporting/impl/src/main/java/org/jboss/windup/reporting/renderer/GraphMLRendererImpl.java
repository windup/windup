package org.jboss.windup.reporting.renderer;

import java.io.File;
import java.io.FileOutputStream;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.jboss.windup.graph.GraphContext;
import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;
import org.jboss.windup.reporting.renderer.api.GraphMLRenderer;


@ApplicationScoped
public class GraphMLRendererImpl implements GraphMLRenderer
{
    @Inject
    private GraphContext context;
    
    @Override
    public void renderGraphML(File outputFile)
    {
        
        GraphMLWriter graphML = new GraphMLWriter(context.getGraph());
        try
        {
            graphML.outputGraph(new FileOutputStream(outputFile));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }   
    }

}
