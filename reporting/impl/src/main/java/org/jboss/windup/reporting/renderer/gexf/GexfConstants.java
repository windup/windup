package org.jboss.windup.reporting.renderer.gexf;

public class GexfConstants {
	public static final String OPENING_TAG = "<gexf xmlns=\"http://www.gexf.net/1.2draft\" version=\"1.2\">";
	public static final String CLOSING_TAG = "</gexf>";
	
	public static final String GRAPH_NODE_OPEN = "<graph mode=\"%1\" defaultedgetype=\"%2\">";
	public static final String GRAPH_NODE_CLOSE = "</graph>";
	
	public static final String NODES_OPEN ="<nodes>";
	public static final String NODES_CLOSE ="</nodes>";
	
	public static final String NODE_TAG ="<node id=\"%1\" label=\"%2\" />";
	public static final String EDGE_TAG ="<edge id=\"%1\" source=\"%2\" target=\"%3\" />";
	
	public static final String EDGES_OPEN ="<edges>";
	public static final String EDGES_CLOSE ="</edges>";
	
	
}
