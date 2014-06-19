package org.jboss.windup.reporting.renderer.graphlib;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.reporting.renderer.GraphWriter;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizDirection;
import org.jboss.windup.reporting.renderer.graphlib.GraphvizConstants.GraphvizType;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class GraphlibWriter implements GraphWriter{
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(GraphlibWriter.class);
	protected final Graph graph;
	protected final GraphvizType type;
	protected final GraphvizDirection direction;
	protected final String graphVariableName;
	protected final String vertexLabelProperty;
	protected final String edgeLabel;
	
	public GraphlibWriter(Graph graph) {
		//default config
		this(graph, GraphvizType.DIGRAPH, GraphvizDirection.TOP_TO_BOTTOM, "g", "label", "label");
	}
	
	public GraphlibWriter(Graph graph, GraphvizType type, GraphvizDirection direction, String graphVariableName, String vertexLabelProperty, String edgeLabelProperty) {
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
		
		final String tag = StringUtils.replaceEach(GraphvizConstants.CONSTRUCTOR_STATEMENT, new String[] {"%NAME", "%TYPE"} , new String[] {graphVariableName, type.getName()});
		IOUtils.write(tag, os);
		
		writeGraphNodes(os);
		writeGraphEdges(os);
		
		IOUtils.write(GraphvizConstants.GRAPH_RENDERER, os);
		IOUtils.write(StringUtils.replace(GraphvizConstants.GRAPH_LAYOUT, "%DIRECTION", direction.getDirection()), os);
		IOUtils.write(StringUtils.replace(GraphvizConstants.GRAPH_RENDERER_RUN, "%NAME", graphVariableName), os);
		
		IOUtils.write(GraphvizConstants.METHOD_CLOSE, os);
	}

	private void writeGraphEdges(OutputStream os) throws IOException {
		int i=0;
		for(Edge edge : graph.getEdges()) {
			String id = ""+i;
			String source = ""+edge.getVertex(Direction.OUT).getId().toString();
			String target = ""+edge.getVertex(Direction.IN).getId().toString();
			
			String label = ""+edge.getProperty(edgeLabel);
			if(edgeLabel != null) {
				label = edgeLabel;
			}

			writeGraphEdge(id, source, target, label, os);
			
			i++;
		}
		
	}

	private void writeGraphEdge(String id, String source, String target, String label, OutputStream os) throws IOException {
		final String tag = StringUtils.replaceEach(GraphvizConstants.EDGE_STATEMENT, 
				new String[] {"%NAME", "%ID", "%SOURCE", "%TARGET", "%LABEL"}, 
				new String[] {graphVariableName, id, source, target, label});
		
		IOUtils.write(tag, os);
	}
	
	private void writeGraphNode(String id, String label, OutputStream os) throws IOException {
		final String tag = StringUtils.replaceEach(GraphvizConstants.NODE_STATEMENT, 
				new String[] {"%NAME", "%ID", "%LABEL", "%CLZLIST"}, 
				new String[] {graphVariableName, id, label, ""});
		
		IOUtils.write(tag, os);
	}
	
	private void writeGraphNodes(OutputStream os) throws IOException {

		//iterate the nodes.
		for(Vertex vertex : graph.getVertices()) {
			String id = vertex.getId().toString();
			String label = vertex.getProperty(vertexLabelProperty);
			writeGraphNode(id, label, os);
		}
		
	}
}
