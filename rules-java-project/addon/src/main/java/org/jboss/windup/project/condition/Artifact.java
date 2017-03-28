package org.jboss.windup.project.condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;

/**
 * Class used to specify the artifact in the {@link Project} condition
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public class Artifact implements Parameterized {
	
	private String groupId;
	private String artifactId;
	private Version version;

	/**
	 * Start with specifying the artifact version
	 */
	public static Artifact withVersion(Version v) {
		Artifact artifact = new Artifact();
		 artifact.version=v;
		 return artifact;
	}

	/**
     * Start with specifying the groupId
     */
	public static Artifact withGroupId(String groupId) {
		
		Artifact artifact = new Artifact();
		 artifact.groupId=groupId;
		 return artifact;
	}

	/**
     * Start with specifying the artifactId
     */
	public static Artifact withArtifactId(String artifactId) {
		Artifact artifact = new Artifact();
		 artifact.artifactId=artifactId;
		 return artifact;
	}

	/**
	 * Specify artifact version
	 * @param version specify the version
	 * @return
	 */
	public Artifact andVersion(Version version) {
		this.version=version;
		return this;
	}

	/**
	 *  Specify artifactId
	 * @param artifactId artifact ID to be set
	 * @return
	 */
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

    @Override
    public Set<String> getRequiredParameterNames()
    {
       return new HashSet<>(Arrays.asList("groupId", "artifactId"));
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
    }
}
