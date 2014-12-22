package org.jboss.windup.project.condition;

public class Artifact {
	
	private String groupId;
	private String artifactId;
	private Version version;

	public static Artifact withVersion(Version v) {
		Artifact artifact = new Artifact();
		 artifact.version=v;
		 return artifact;
	}

	public static Artifact withGroupId(String groupId) {
		
		Artifact artifact = new Artifact();
		 artifact.groupId=groupId;
		 return artifact;
	}

	public static Artifact withArtifactId(String artifactId) {
		Artifact artifact = new Artifact();
		 artifact.artifactId=artifactId;
		 return artifact;
	}

	public Artifact andVersion(Version version) {
		this.version=version;
		return this;
	}

	public Artifact andGroupId(String groupId) {
		this.groupId=groupId;
		return this;
	}

	public Artifact andArtifactId(String artifactId) {
		this.artifactId=artifactId;
		return this;

	}
	
	public String getGroupId() {
		return groupId;
	}

	

	public String getArtifactId() {
		return artifactId;
	}

	
	public Version getVersion() {
		return version;
	}

}
