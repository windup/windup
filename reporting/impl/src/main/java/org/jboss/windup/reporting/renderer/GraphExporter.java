package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.renderer.dot.VizJSHtmlWriter;
import org.jboss.windup.reporting.renderer.gexf.SigmaJSHtmlWriter;
import org.jboss.windup.reporting.renderer.graphlib.DagreD3JSHtmlWriter;
import org.jboss.windup.util.Logging;

public class GraphExporter extends AbstractGraphRenderer {
    private static final java.util.logging.Logger LOG = Logging.get(GraphExporter.class);

    @Override
    public void renderGraph(GraphContext context) {
        Graph graph = context.getGraph();

        Path vizJSOutPath = createOutputFolder(context, "visjs");
        Path sigmaOutPath = createOutputFolder(context, "sigma");
        Path dagreD3OutPath = createOutputFolder(context, "dagred3");

        renderVizjs(graph, vizJSOutPath, "label", "id");
        renderSigma(graph, sigmaOutPath, "label", "id");
        renderDagreD3(graph, dagreD3OutPath, "label", "id");
    }

    public void renderVizjs(Graph graph, Path output, String vertexLabelProperty, String edgeLabel) {
        LOG.fine("Writing Vizjs graph to: " + output.toAbsolutePath());
        try {
            new VizJSHtmlWriter(graph, vertexLabelProperty, edgeLabel).writeGraph(output);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to write graph visualization due to: " + e.getMessage(), e);
        }
    }

    public void renderSigma(Graph graph, Path output, String vertexLabelProperty, String edgeLabel) {
        LOG.fine("Writing Sigmajs graph to: " + output.toAbsolutePath());
        try {
            new SigmaJSHtmlWriter(graph, vertexLabelProperty, edgeLabel).writeGraph(output);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to write graph visualization due to: " + e.getMessage(), e);
        }
    }

    public void renderDagreD3(Graph graph, Path output, String vertexLabelProperty, String edgeLabel) {
        LOG.fine("Writing DagreD3 graph to: " + output.toAbsolutePath());
        try {
            new DagreD3JSHtmlWriter(graph, vertexLabelProperty, edgeLabel).writeGraph(output);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Failed to write graph visualization due to: " + e.getMessage(), e);
        }
    }
}
