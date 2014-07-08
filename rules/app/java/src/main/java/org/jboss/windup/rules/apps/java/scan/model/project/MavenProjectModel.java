package org.jboss.windup.rules.apps.java.scan.model.project;

import org.jboss.windup.graph.model.meta.xml.XmlMetaFacetModel;
import org.jboss.windup.reporting.renderer.api.Label;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Extends ProjectModel to support maven specific properties.
 * 
 * @author jsightler
 * 
 */
@TypeValue("MavenFacet")
public interface MavenProjectModel extends ProjectModel, XmlMetaFacetModel
{

    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_ARTIFACT_ID = "artifactId";
    public static final String PROPERTY_GROUP_ID = "groupId";
    public static final String PROPERTY_SPECIFICATION_VERSION = "specificationVersion";
    public static final String PROPERTY_MAVEN_IDENTIFIER = "mavenIdentifier";

    /**
     * The full maven id (groupid, artifactid, and version)
     * 
     */
    @Label
    @Property(PROPERTY_MAVEN_IDENTIFIER)
    public String getMavenIdentifier();

    @Property(PROPERTY_MAVEN_IDENTIFIER)
    public void setMavenIdentifier(String identifier);

    /**
     * The Maven specification version
     * 
     */
    @Label
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
