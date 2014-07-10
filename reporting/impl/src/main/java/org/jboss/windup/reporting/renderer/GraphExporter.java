package org.jboss.windup.reporting.renderer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import org.jboss.windup.reporting.renderer.dot.VizJSHtmlWriter;
import org.jboss.windup.reporting.renderer.gexf.SigmaJSHtmlWriter;
import org.jboss.windup.reporting.renderer.graphlib.DagreD3JSHtmlWriter;
import org.jboss.windup.util.exception.WindupException;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;

public class GraphExporter extends AbstractGraphRenderer
{
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GraphExporter.class);

    @Override
    public void renderGraph()
    {
        Graph graph = getContext().getGraph();

        Path vizJSOutFile = createOutputFolder("visjs").resolve("index.html");
        Path sigmaOutFile = createOutputFolder("sigma").resolve("index.html");
        Path dagreD3OutFile = createOutputFolder("dagred3").resolve("index.html");

        renderVizjs(graph, vizJSOutFile.toFile(), "label", "id");
        renderSigma(graph, sigmaOutFile.toFile(), "label", "id");
        renderDagreD3(graph, dagreD3OutFile.toFile(), "label", "id");
    }

    public void renderVizjs(Graph graph, File output, String vertexLabelProperty, String edgeLabel)
    {
        LOG.debug("Writing Vizjs graph to: " + output.getAbsolutePath());
        render(new VizJSHtmlWriter(graph, vertexLabelProperty, edgeLabel), output);
    }

    public void renderSigma(Graph graph, File output, String vertexLabelProperty, String edgeLabel)
    {
        LOG.debug("Writing Sigmajs graph to: " + output.getAbsolutePath());
        render(new SigmaJSHtmlWriter(graph, vertexLabelProperty, edgeLabel), output);
    }

    public void renderDagreD3(Graph graph, File output, String vertexLabelProperty, String edgeLabel)
    {
        LOG.debug("Writing DagreD3 graph to: " + output.getAbsolutePath());
        render(new DagreD3JSHtmlWriter(graph, vertexLabelProperty, edgeLabel), output);
    }

    private void render(GraphWriter writer, File output)
    {
        try (FileOutputStream fos = new FileOutputStream(output))
        {
            writer.writeGraph(fos);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to render report due to: " + e.getMessage(), e);
        }
    }
}
