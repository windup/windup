package org.jboss.windup.reporting.renderer.gexf;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jboss.windup.reporting.renderer.GraphDataSerializer;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GexfWriter implements GraphDataSerializer {

    protected final Graph graph;
    protected String defaultEdgeType = "directed";
    protected String mode = "static";
    protected String vertexLabelProperty = "label";
    protected String edgeLabel = null;

    public GexfWriter(Graph graph) {
        this.graph = graph;
    }

    public GexfWriter(Graph graph, String mode, String defaultEdgeType, String vertexLabelProperty, String edgeLabel) {
        this.graph = graph;
        this.mode = mode;
        this.defaultEdgeType = defaultEdgeType;
        this.vertexLabelProperty = vertexLabelProperty;
        this.edgeLabel = edgeLabel;
    }

    public void writeGraph(OutputStream os) throws IOException {
        writeGexf(os);
    }

    private void writeGexf(OutputStream os) throws IOException {
        IOUtils.write(GexfConstants.OPENING_TAG, os);
        writeGraphTag(mode, defaultEdgeType, os);
        IOUtils.write(GexfConstants.CLOSING_TAG, os);
    }

    private void writeGraphTag(String mode, String edgeType, OutputStream os) throws IOException {
        final String tag = StringUtils.replaceEach(GexfConstants.GRAPH_NODE_OPEN, new String[]{"%1", "%2"},
                new String[]{mode, edgeType});
        IOUtils.write(tag, os);

        writeGraphNodes(os);
        writeGraphEdges(os);

        IOUtils.write(GexfConstants.GRAPH_NODE_CLOSE, os);
    }

    private void writeGraphEdges(OutputStream os) throws IOException {
        IOUtils.write(GexfConstants.EDGES_OPEN, os);

        Iterator<Edge> edgeIterator = graph.edges();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            String id = "" + edge.id().hashCode();
            String source = "" + edge.outVertex().id().toString();
            String target = "" + edge.inVertex().id().toString();
            writeGraphEdge(id, source, target, os);
        }

        IOUtils.write(GexfConstants.EDGES_CLOSE, os);
    }

    private void writeGraphEdge(String id, String source, String target, OutputStream os) throws IOException {
        final String tag = StringUtils.replaceEach(GexfConstants.EDGE_TAG, new String[]{"%1", "%2", "%3"},
                new String[]{id, source, target});
        IOUtils.write(tag, os);
    }

    private void writeGraphNode(String id, String label, OutputStream os) throws IOException {
        final String tag = StringUtils.replaceEach(GexfConstants.NODE_TAG,
                new String[]{"%1", "%2"},
                new String[]{id, label});
        IOUtils.write(tag, os);
    }

    private void writeGraphNodes(OutputStream os) throws IOException {

        IOUtils.write(GexfConstants.NODES_OPEN, os);
        // iterate the nodes.
        Iterator<Vertex> vertexIterator = graph.vertices();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            String id = "" + vertex.id().toString();
            String label = (String) vertex.property(vertexLabelProperty).value();

            if (StringUtils.isBlank(label)) {
                label = vertex.toString();
            }
            writeGraphNode(id, label, os);
        }
        IOUtils.write(GexfConstants.NODES_CLOSE, os);

    }

}
