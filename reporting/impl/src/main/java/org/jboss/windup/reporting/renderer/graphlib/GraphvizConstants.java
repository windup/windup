package org.jboss.windup.reporting.renderer.graphlib;

public class GraphvizConstants {

	public static final String METHOD_OPEN = "function createGraph() {\n";
	public static final String METHOD_CLOSE = "}";
	public static final String TAB = "\t";
	public static final String CONSTRUCTOR_STATEMENT = TAB+"var %NAME = new dagreD3.%TYPE();\n";
	public static final String EDGE_STATEMENT = TAB+"%NAME.addEdge(%ID, %SOURCE, %TARGET, { label: \"%LABEL\" });\n";
	public static final String NODE_STATEMENT = TAB+"%NAME.addNode(%ID, { label: \"%LABEL\", class: \"%CLZLIST\" });\n";
	public static final String GRAPH_LAYOUT = TAB+"var layout = dagreD3.layout().nodeSep(20).rankDir(\"%DIRECTION\");\n";
	public static final String GRAPH_RENDERER = TAB+"var renderer = new dagreD3.Renderer();\n";
	public static final String GRAPH_RENDERER_RUN = TAB+"renderer.run(%NAME, d3.select(\"svg g\"));\n";
	
	
	public enum GraphvizDirection {
		TOP_TO_BOTTOM("TB"),
		LEFT_TO_RIGHT("LR");
		
		private final String direction;
		
		private GraphvizDirection(String direction) {
			this.direction = direction;
		}
		
		public String getDirection() {
			return direction;
		}
	}
	
	public enum GraphvizType {
		DIGRAPH("Digraph"),
		UNDIRECTED_GRAPH("Graph"),
		CP_UNDIRECTED_GRAPH("CGraph"),
		CP_DIRECTED_GRAPH("CDGraph");
		
		private final String name;
		
		private GraphvizType(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
}
