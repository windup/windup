package org.jboss.windup.rules.apps.java.model.project;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Extends ProjectModel to support maven specific properties.
 * 
 * @author jsightler
 * 
 */
@TypeValue(MavenProjectModel.TYPE)
public interface MavenProjectModel extends ProjectModel
{
    public static final String MAVEN_POM = "mavenPom";
    public static final String PARENT_MAVEN_POM = "parentMavenPOM";
    public static final String TYPE = "MavenFacet";
    public static final String ARTIFACT_ID = "artifactId";
    public static final String GROUP_ID = "groupId";
    public static final String SPECIFICATION_VERSION = "specificationVersion";
    public static final String MAVEN_IDENTIFIER = "mavenIdentifier";

    /**
     * Contains the parent POM (if available).
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.OUT)
    MavenProjectModel getParentMavenPOM();

    /**
     * Contains the parent POM (if available).
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.OUT)
    void setParentMavenPOM(MavenProjectModel parentMavenProject);

    /**
     * Gets projects that have this POM set as their maven parent
     */
    @Adjacency(label = PARENT_MAVEN_POM, direction = Direction.IN)
    Iterable<MavenProjectModel> getMavenChildProjects();

    /**
     * Contains the maven pom {@link XmlFileModel}.
     */
    @Adjacency(label = MAVEN_POM, direction = Direction.OUT)
    Iterable<XmlFileModel> getMavenPom();

    /**
     * Contains the maven pom {@link XmlFileModel}.
     */
    @Adjacency(label = MAVEN_POM, direction = Direction.OUT)
    void addMavenPom(XmlFileModel pom);

    /**
     * The full maven id (groupid, artifactid, and version).
     */
    @Property(MAVEN_IDENTIFIER)
    String getMavenIdentifier();

    /**
     * The full maven id (groupid, artifactid, and version).
     */
    @Property(MAVEN_IDENTIFIER)
    void setMavenIdentifier(String identifier);

    /**
     * The Maven specification version.
     */
    @Property(SPECIFICATION_VERSION)
    String getSpecificationVersion();

    /**
     * The Maven specification version.
     */
    @Property(SPECIFICATION_VERSION)
    void setSpecificationVersion(String version);

    /**
     * Contains the maven group id.
     */
    @Property(GROUP_ID)
    String getGroupId();

    /**
     * Contains the maven group id.
     */
    @Property(GROUP_ID)
    void setGroupId(String version);

    /**
     * Contains the maven artifact id.
     */
    @Property(ARTIFACT_ID)
    String getArtifactId();

    /**
     * Contains the maven artifact id.
     */
    @Property(ARTIFACT_ID)
    void setArtifactId(String artifactId);
}
