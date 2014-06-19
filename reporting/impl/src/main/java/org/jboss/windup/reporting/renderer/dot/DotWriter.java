package org.jboss.windup.reporting.renderer.dot;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.reporting.renderer.GraphWriter;
import org.jboss.windup.reporting.renderer.dot.DotConstants.DotGraphType;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

public class DotWriter implements GraphWriter {
	
	private final Graph graph;
	private String graphName = "G";
	private String vertexLabelProperty = "label";
	private DotGraphType graphType = DotGraphType.DIGRAPH;
	private String fontSize = "12pt";
	private String edgeLabel = "";
	
	public DotWriter(Graph graph) {
		this.graph = graph;
	}

	public DotWriter(Graph graph, String graphName, String vertexLabelProperty, String edgeLabel, DotGraphType graphType, String fontSize) {
		this.graph = graph;
		this.graphName = graphName;
		this.fontSize = fontSize;
		this.vertexLabelProperty = vertexLabelProperty;
		this.graphType = graphType;
		this.edgeLabel = edgeLabel;
	}

	
	@Override
	public void writeGraph(OutputStream os) throws IOException {
		writeDot(os);
	}
	
	private void writeDot(OutputStream os) throws IOException {
		writeGraphTag(os);
	}
	
	private void writeGraphTag(OutputStream os) throws IOException {
		String name = this.getDotSafeName(graphName);
		IOUtils.write(graphType.getName()+" "+name+"{" + DotConstants.NL, os);
		
		writeGraphNodes(os);
		writeGraphEdges(os);
		
		IOUtils.write("}", os);
	}

	private void writeGraphEdges(OutputStream os) throws IOException {
		for(Edge edge : graph.getEdges()) {
			String label = edgeLabel;
			String source = ""+edge.getVertex(Direction.OUT).getId().toString();
			String target = ""+edge.getVertex(Direction.IN).getId().toString();
			writeGraphEdge(label, source, target, os);
		}
	}

	private void writeGraphEdge(String label, String source, String target, OutputStream os) throws IOException {
		final String startTag = getDotSafeName(source) + graphType.getEdge() + getDotSafeName(target);
		final String endTag = DotConstants.END_LINE;
		
		IOUtils.write(startTag, os);
		
		if(StringUtils.isNotBlank(label)) {
			writeOptions(os, new String[] {"label", label}, new String[]{"fontsize", fontSize});
		}
		
		IOUtils.write(endTag, os);
		
	}
	
	private void writeGraphNode(String id, String label, OutputStream os) throws IOException {
		final String tag = DotConstants.INDENT + getDotSafeName(id) + "[label = \""+label+"\", fontsize = \""+fontSize+"\"]" + DotConstants.END_LINE;
		IOUtils.write(tag, os);
	}
	
	private void writeGraphNodes(OutputStream os) throws IOException {

		//iterate the nodes.
		for(Vertex vertex : graph.getVertices()) {
			String id = ""+vertex.getId().toString();
			String label = vertex.getProperty(vertexLabelProperty);
			
			if(StringUtils.isBlank(label)) {
				label = vertex.toString();
			}
			writeGraphNode(id, label, os);
		}
		
	}

	private void writeOptions(OutputStream os, String[]... options) throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		for(String[] option : options) {
			String key = option[0];
			String value = option[1];
			map.put(key, value);
			
		}
		writeOptions(map, os);
	}
	
	private void writeOptions(Map<String, String> options, OutputStream os) throws IOException {
		if(options == null || options.size() == 0) {
			return;
		}
		
		IOUtils.write("[", os);
		
		StringBuilder builder = new StringBuilder();
		for(String key : options.keySet()) {
			builder.append(key + "=\"" +options.get(key) +"\", ");
		}
		String tag = builder.toString().trim();
		tag = StringUtils.removeEnd(tag, ",");
		
		IOUtils.write(tag, os);
		IOUtils.write("]", os);
	}
	
	private String getDotSafeName(String inName) {
		String name = null;
		if(StringUtils.isAlphanumeric(inName)) {
			name = inName;
		}
		else {
			name = "\""+inName+"\"";
		}
		
		return name;
	}

}
