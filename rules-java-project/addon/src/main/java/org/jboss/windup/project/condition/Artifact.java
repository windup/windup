package org.jboss.windup.project.condition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.graph.model.DependencyLocation;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.Parameterized;
import org.ocpsoft.rewrite.param.ParameterizedPatternParser;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;

/**
 * Class used to specify the artifact in the {@link Project} condition
 * 
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 *
 */
public class Artifact implements Parameterized
{

    private RegexParameterizedPatternParser groupId;
    private RegexParameterizedPatternParser artifactId;
    private Version version;
    private Collection<DependencyLocation> locations;

    /**
     * Start with specifying the artifact version
     */
    public static Artifact withVersion(Version v)
    {
        Artifact artifact = new Artifact();
        artifact.version = v;
        return artifact;
    }

    /**
     * Start with specifying the groupId
     */
    public static Artifact withGroupId(String groupId)
    {

        Artifact artifact = new Artifact();
        artifact.groupId = new RegexParameterizedPatternParser(groupId);
        return artifact;
    }

    /**
     * Start with specifying the artifactId
     */
    public static Artifact withArtifactId(String artifactId)
    {
        Artifact artifact = new Artifact();
        artifact.artifactId = new RegexParameterizedPatternParser(artifactId);
        return artifact;
    }

    /**
     * Specify artifact version
     * 
     * @param version specify the version
     * @return
     */
    public Artifact andVersion(Version version)
    {
        this.version = version;
        return this;
    }

    /**
     * Specify artifactId
     * 
     * @param artifactId artifact ID to be set
     * @return
     */
    public Artifact andArtifactId(String artifactId)
    {
        this.artifactId = new RegexParameterizedPatternParser(artifactId);
        return this;

    }

    public Artifact andLocations(Collection<DependencyLocation> locations) {
        this.locations = locations;
        return this;
    }

    public ParameterizedPatternParser getGroupId()
    {
        return groupId;
    }

    public ParameterizedPatternParser getArtifactId()
    {
        return artifactId;
    }

    public Version getVersion()
    {
        return version;
    }

    public Collection<DependencyLocation> getLocations() {
        return locations;
    }

    @Override
    public Set<String> getRequiredParameterNames()
    {
        Set<String> result = new HashSet<>();
        if (groupId != null) result.addAll(groupId.getRequiredParameterNames());
        if (artifactId != null) result.addAll(artifactId.getRequiredParameterNames());
        return result;
    }

    @Override
    public void setParameterStore(ParameterStore store)
    {
		if (groupId != null)
			groupId.setParameterStore(store);

		if (artifactId != null)
			artifactId.setParameterStore(store);
    }
}
