package org.jboss.windup.reporting.renderer.graphlib;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.jboss.windup.reporting.renderer.GraphDataSerializer;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizDirection;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizType;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

public class GraphlibWriter implements GraphDataSerializer {
    protected final Graph graph;
    protected final GraphvizType type;
    protected final GraphvizDirection direction;
    protected final String graphVariableName;
    protected final String vertexLabelProperty;
    protected final String edgeLabel;

    public GraphlibWriter(Graph graph) {
        // default config
        this(graph, GraphvizType.DIGRAPH, GraphvizDirection.TOP_TO_BOTTOM, "g", "label", "label");
    }

    public GraphlibWriter(Graph graph, GraphvizType type, GraphvizDirection direction, String graphVariableName,
                          String vertexLabelProperty, String edgeLabelProperty) {
        this.graph = graph;
        this.graphVariableName = graphVariableName;
        this.vertexLabelProperty = vertexLabelProperty;
        this.edgeLabel = edgeLabelProperty;
        this.type = type;
        this.direction = direction;
    }

    @Override
    public void writeGraph(OutputStream os) throws IOException {
        IOUtils.write(GraphvizConstants.METHOD_OPEN, os);

        final String tag = StringUtils.replaceEach(GraphvizConstants.CONSTRUCTOR_STATEMENT, new String[]{"%NAME",
                "%TYPE"}, new String[]{graphVariableName, type.getName()});
        IOUtils.write(tag, os);

        writeGraphNodes(os);
        writeGraphEdges(os);

        IOUtils.write(GraphvizConstants.GRAPH_RENDERER, os);
        IOUtils.write(StringUtils.replace(GraphvizConstants.GRAPH_LAYOUT, "%DIRECTION", direction.getDirection()), os);
        IOUtils.write(StringUtils.replace(GraphvizConstants.GRAPH_RENDERER_RUN, "%NAME", graphVariableName), os);

        IOUtils.write(GraphvizConstants.METHOD_CLOSE, os);
    }

    private void writeGraphEdges(OutputStream os) throws IOException {
        int i = 0;
        Iterator<Edge> edgeIterator = graph.edges();
        while (edgeIterator.hasNext()) {
            Edge edge = edgeIterator.next();
            String id = "" + i;
            String source = "" + edge.outVertex().id().toString();
            String target = "" + edge.inVertex().id().toString();

            String label = "" + edge.property(edgeLabel);
            if (edgeLabel != null) {
                label = edgeLabel;
            }

            writeGraphEdge(id, source, target, label, os);

            i++;
        }

    }

    private void writeGraphEdge(String id, String source, String target, String label, OutputStream os)
            throws IOException {
        final String tag = StringUtils.replaceEach(GraphvizConstants.EDGE_STATEMENT,
                new String[]{"%NAME", "%ID", "%SOURCE", "%TARGET", "%LABEL"},
                new String[]{graphVariableName, id, source, target, label});

        IOUtils.write(tag, os);
    }

    private void writeGraphNode(String id, String label, OutputStream os) throws IOException {
        final String tag = StringUtils.replaceEach(GraphvizConstants.NODE_STATEMENT,
                new String[]{"%NAME", "%ID", "%LABEL", "%CLZLIST"},
                new String[]{graphVariableName, id, label, ""});

        IOUtils.write(tag, os);
    }

    private void writeGraphNodes(OutputStream os) throws IOException {
        // iterate the nodes.
        Iterator<Vertex> vertexIterator = graph.vertices();
        while (vertexIterator.hasNext()) {
            Vertex vertex = vertexIterator.next();
            String id = vertex.id().toString();
            String label = (String) vertex.property(vertexLabelProperty).value();
            writeGraphNode(id, label, os);
        }

    }
}
