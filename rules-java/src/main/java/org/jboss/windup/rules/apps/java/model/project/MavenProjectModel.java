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
@TypeValue("MavenFacet")
public interface MavenProjectModel extends ProjectModel
{
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_ARTIFACT_ID = "artifactId";
    public static final String PROPERTY_GROUP_ID = "groupId";
    public static final String PROPERTY_SPECIFICATION_VERSION = "specificationVersion";
    public static final String PROPERTY_MAVEN_IDENTIFIER = "mavenIdentifier";

    @Adjacency(label = "parentMavenPOM", direction = Direction.OUT)
    public MavenProjectModel getParentMavenPOM();

    @Adjacency(label = "parentMavenPOM", direction = Direction.OUT)
    public void setParentMavenPOM(MavenProjectModel parentMavenProject);

    @Adjacency(label = "parentMavenPOM", direction = Direction.IN)
    public Iterable<MavenProjectModel> getMavenChildProjects();

    @Adjacency(label = "mavenPom", direction = Direction.OUT)
    public Iterable<XmlFileModel> getMavenPom();

    @Adjacency(label = "mavenPom", direction = Direction.OUT)
    public void addMavenPom(XmlFileModel pom);

    /**
     * The full maven id (groupid, artifactid, and version)
     * 
     */
    @Property(PROPERTY_MAVEN_IDENTIFIER)
    public String getMavenIdentifier();

    @Property(PROPERTY_MAVEN_IDENTIFIER)
    public void setMavenIdentifier(String identifier);

    /**
     * The Maven specification version
     * 
     */
    @Property(PROPERTY_SPECIFICATION_VERSION)
    public String getSpecificationVersion();

    @Property(PROPERTY_SPECIFICATION_VERSION)
    public void setSpecificationVersion(String version);

    @Property(PROPERTY_GROUP_ID)
    public String getGroupId();

    @Property(PROPERTY_GROUP_ID)
    public void setGroupId(String version);

    @Property(PROPERTY_ARTIFACT_ID)
    public String getArtifactId();

    @Property(PROPERTY_ARTIFACT_ID)
    public void setArtifactId(String artifactId);

    @Property(PROPERTY_URL)
    public String getURL();

    @Property(PROPERTY_URL)
    public void setURL(String url);
}
