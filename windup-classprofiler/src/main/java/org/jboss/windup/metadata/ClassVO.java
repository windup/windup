package org.jboss.windup.metadata;

import java.util.List;

public class ClassVO {

	private String qualifiedName;
	private String className;
	private String packageName;
	private List<String> dependencies;
	
	public String getQualifiedName() {
		return qualifiedName;
	}
	public void setQualifiedName(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public List<String> getDependencies() {
		return dependencies;
	}
	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}
	@Override
	public String toString() {
		return "ClassVO [qualifiedName=" + qualifiedName + ", className="
				+ className + ", packageName=" + packageName
				+ ", dependencies=" + dependencies + "]";
	}
	
	
}
