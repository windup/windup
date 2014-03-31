package org.jboss.windup.graph.renderer.graphlib;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jboss.windup.graph.renderer.GraphWriter;
import org.jboss.windup.graph.renderer.graphlib.GraphvizConstants.GraphvizDirection;
import org.jboss.windup.graph.renderer.graphlib.GraphvizConstants.GraphvizType;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.minlog.Log;
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
	
	public GraphlibWriter(Graph graph) {
		//default config
		this(graph, GraphvizType.DIGRAPH, GraphvizDirection.TOP_TO_BOTTOM, "g", "label");
	}
	
	public GraphlibWriter(Graph graph, GraphvizType type, GraphvizDirection direction, String graphVariableName, String vertexLabelProperty) {
		this.graph = graph;
		this.graphVariableName = graphVariableName;
		this.vertexLabelProperty = vertexLabelProperty;
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
		
		for(Edge edge : graph.getEdges()) {
			String id = ""+edge.getId().hashCode();
			String source = ""+edge.getVertex(Direction.IN).getId().hashCode();
			String target = ""+edge.getVertex(Direction.OUT).getId().hashCode();
			String label = ""+edge.getLabel();
			writeGraphEdge(id, source, target, label, os);
		}
		
	}

	private void writeGraphEdge(String id, String source, String target, String label, OutputStream os) throws IOException {
		final String tag = StringUtils.replaceEach(GraphvizConstants.EDGE_STATEMENT, 
				new String[] {"%NAME", "%ID", "%SOURCE", "%TARGET", "%LABEL"}, 
				new String[] {graphVariableName, id, source, target, label});
		
		IOUtils.write(tag, os);
	}
	
	private void writeGraphNode(String id, String label, Set<String> classes, OutputStream os) throws IOException {
		StringBuilder clzBuilder = new StringBuilder();
		for(String clz : classes) {
			clzBuilder.append(clz+", ");
		}
		String clzList = StringUtils.removeEnd(clzBuilder.toString(), ", ");
		LOG.debug("Classlist: "+clzList);
		
		
		final String tag = StringUtils.replaceEach(GraphvizConstants.NODE_STATEMENT, 
				new String[] {"%NAME", "%ID", "%LABEL", "%CLZLIST"}, 
				new String[] {graphVariableName, id, label, clzList});
		
		IOUtils.write(tag, os);
	}
	
	private void writeGraphNodes(OutputStream os) throws IOException {

		//iterate the nodes.
		for(Vertex vertex : graph.getVertices()) {
			String id = ""+vertex.getId().hashCode();
			String label = vertex.getProperty(vertexLabelProperty);
			
			Set<String> clzSet = new HashSet<String>();
			Boolean blacklist = (Boolean)vertex.getProperty("blacklist");
			if(blacklist != null && blacklist) {
				clzSet.add("blacklisted");
			}
			
			if(StringUtils.isBlank(label)) {
				label = vertex.toString();
			}
			writeGraphNode(id, label, clzSet, os);
		}
		
	}
}
