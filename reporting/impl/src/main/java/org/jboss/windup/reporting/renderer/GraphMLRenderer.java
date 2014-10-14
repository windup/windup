package org.jboss.windup.reporting.renderer;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.WindupConfigurationService;

import com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter;

public class GraphMLRenderer extends AbstractGraphRenderer
{
    @Override
    public void renderGraph(GraphContext context)
    {
        WindupConfigurationModel configuration = WindupConfigurationService.getConfigurationModel(context);

        Path outputFolder = createOutputFolder(configuration, "graphml");
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
