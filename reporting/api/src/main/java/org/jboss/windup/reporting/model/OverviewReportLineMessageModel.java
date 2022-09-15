package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.config.Rule;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

/**
 * This classifies files and provides general background information about a specific {@link FileModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link ClassificationModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(OverviewReportLineMessageModel.TYPE)
public interface OverviewReportLineMessageModel extends WindupVertexFrame {
    String TYPE = "OverviewReportLineMessageModel";
    String PROPERTY_RULE_ID = TYPE + "-ruleID";
    String PROJECT_MODEL = TYPE + "-project";
    String PROPERTY_MESSAGE = TYPE + "-message";

    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    void setProject(ProjectModel pModel);

    /**
     * Get the {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProject();

    /**
     * Set message
     */
    @Property(PROPERTY_MESSAGE)
    void setMessage(String message);

    /**
     * Get message
     */
    @Property(PROPERTY_MESSAGE)
    String getMessage();

    /**
     * Set ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(PROPERTY_RULE_ID)
    void setRuleID(String ruleID);

    /**
     * Get ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(PROPERTY_RULE_ID)
    String getRuleID();

}
