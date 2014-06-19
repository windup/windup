package org.jboss.windup.reporting.renderer.dot;

import java.util.HashMap;
import java.util.Map;

public class DotConstants {

	public static final String INDENT =" \t";
	public static final String NL ="\n";
	public static final String END_LINE =";\n";
	
	public enum DotGraphType {
		DIGRAPH("digraph", "->"),
		GRAPH("graph", "--");
		
		private final String name;
		private final String edge;
		
		private DotGraphType(String name, String edge) {
			this.name = name;
			this.edge = edge;
		}
		
		public String getEdge() {
			return edge;
		}
		
		public String getName() {
			return name;
		}
	}
	
	public static class Options {
		public static Map<String, String> create() {
			return new HashMap<String, String>();
		}
	}
	
}
