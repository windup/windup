package org.jboss.windup.reporting.renderer;

import java.io.FileOutputStream;
import java.nio.file.Path;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class GraphMLRenderer extends AbstractGraphRenderer
{
    @Override
    public void renderGraph()
    {
        Path outputFolder = createOutputFolder("graphml");

        Path outputFile = outputFolder.resolve("graph.graphml");

        GraphMLWriter graphML = new GraphMLWriter(getContext().getGraph());
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
