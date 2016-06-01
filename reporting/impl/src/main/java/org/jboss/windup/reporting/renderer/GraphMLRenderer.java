package org.jboss.windup.reporting.renderer;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.jboss.windup.graph.GraphContext;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class GraphMLRenderer extends AbstractGraphRenderer
{
    @Override
    public void renderGraph(GraphContext context)
    {
        Path outputFolder = createOutputFolder(context, "graphml");
        Path outputFile = outputFolder.resolve("graph.graphml");

        GraphMLWriter graphML = new GraphMLWriter(context.getGraph());
        try
        {
            graphML.outputGraph(new FileOutputStream(outputFile.toFile()));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
