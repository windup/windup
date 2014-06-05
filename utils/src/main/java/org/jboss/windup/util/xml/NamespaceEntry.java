package org.jboss.windup.util.xml;

public class NamespaceEntry {

	private final String prefix;
	private final String namespaceURI;
	
	public NamespaceEntry(String prefix, String namespaceURI) {
		this.prefix = prefix;
		this.namespaceURI = namespaceURI;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getNamespaceURI() {
		return namespaceURI;
	}

	@Override
	public String toString() {
		return "NamespaceEntry [prefix=" + prefix + ", namespaceURI=" + namespaceURI + "]";
	}
}
