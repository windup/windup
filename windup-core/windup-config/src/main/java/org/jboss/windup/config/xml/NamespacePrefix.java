package org.jboss.windup.config.xml;


public class NamespacePrefix {

	private String prefix;
	private String namespace;
	
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	@Override
	public String toString() {
		return "NamespacePrefix [prefix=" + prefix + ", namespace=" + namespace + "]";
	}
}
