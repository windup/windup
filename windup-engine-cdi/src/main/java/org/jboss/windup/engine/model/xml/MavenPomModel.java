package org.jboss.windup.engine.model.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.eclipse.persistence.oxm.annotations.XmlPath;


@XmlRootElement(name="project")
@XmlType(namespace="http://maven.apache.org/POM/4.0.0")
public class MavenPomModel {
	
	@XmlElement
	public String groupId;
	
	@XmlElement
	public String artifactId;
	
	@XmlElement
	public String version;
	
	@XmlElement
	private Parent parent;
	
	@XmlPath("dependencies/dependency")
	private List<Dependency> dependencies;
	
	
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	

	
	@XmlRootElement(name="dependency", namespace="http://maven.apache.org/POM/4.0.0")
	public static class Dependency {
		@XmlElement
		public String groupId;
		
		@XmlElement
		public String artifactId;
		
		@XmlElement
		public String version;
		
	}
	

	@XmlRootElement(name="parent", namespace="http://maven.apache.org/POM/4.0.0")
	public static class Parent {
		@XmlElement
		public String groupId;
		
		@XmlElement
		public String artifactId;
		
		@XmlElement
		public String version;
		
	}
}