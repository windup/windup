package org.jboss.windup.classprofiler.metadata;

import java.util.LinkedList;
import java.util.List;

public class ClassVO {

	private String qualifiedName;
	private List<String> dependencies = new LinkedList<String>();
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}
	public List<String> getDependencies() {
		return dependencies;
	}
	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}
	
	@Override
	public String toString() {
		return "ClassVO [qualifiedName=" + qualifiedName + ", dependencies="
				+ dependencies + "]";
	}
}
