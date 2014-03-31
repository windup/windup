package org.jboss.windup.metadata.decoration;

import org.w3c.dom.Node;

public class XmlLine extends Line {

	private Node matchedNode;
	
	public void setMatchedNode(Node matchedNode) {
		this.matchedNode = matchedNode;
	}
	
	public Node getMatchedNode() {
		return matchedNode;
	}
}
