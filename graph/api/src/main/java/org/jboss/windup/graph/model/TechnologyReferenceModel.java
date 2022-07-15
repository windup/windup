package org.jboss.windup.graph.model;

import org.jboss.windup.graph.Property;

/**
 * This stores a technology reference (id and version range) in the graph.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TechnologyReferenceModel.TYPE)
public interface TechnologyReferenceModel extends WindupVertexFrame {
    String TYPE = "TechnologyReferenceModel";
    String TECHNOLOGY_ID = "technologyID";
    String VERSION_RANGE = "versionRange";

    /**
     * Contains the technology id
     */
    @Property(TECHNOLOGY_ID)
    String getTechnologyID();

    /**
     * Contains the technology id
     */
    @Property(TECHNOLOGY_ID)
    void setTechnologyID(String technologyID);

    /**
     * Contains the version range
     */
    @Property(VERSION_RANGE)
    String getVersionRange();

    /**
     * Contains the version range
     */
    @Property(VERSION_RANGE)
    void setVersionRange(String versionRange);
}
