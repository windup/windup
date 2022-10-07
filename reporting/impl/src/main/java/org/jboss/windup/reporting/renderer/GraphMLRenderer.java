package org.jboss.windup.reporting.renderer;

import java.io.FileOutputStream;
import java.nio.file.Path;

import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLWriter;
import org.jboss.windup.graph.GraphContext;

public class GraphMLRenderer extends AbstractGraphRenderer {
    @Override
    public void renderGraph(GraphContext context) {
        Path outputFolder = createOutputFolder(context, "graphml");
        Path outputFile = outputFolder.resolve("graph.graphml");

        GraphMLWriter graphML = GraphMLWriter.build().create();
        try {
            graphML.writeGraph(new FileOutputStream(outputFile.toFile()), context.getGraph());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
