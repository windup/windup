package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.config.Rule;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This classifies files and provides general background information about a specific {@link FileModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link ClassificationModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(OverviewReportLineMessageModel.TYPE)
public interface OverviewReportLineMessageModel extends WindupVertexFrame
{
    public static final String TYPE = "OverviewReportLineMessageModel";
    public static final String PROPERTY_RULE_ID = "ruleID";
    public static final String PROJECT_MODEL = "project";
    public static final String PROPERTY_MESSAGE = "message";
    public static final String PROPERTY_EFFORT = "effort";
    public static final String PROPERTY_LINKS = "links";

    public static final String FILE_MODEL = "classificationModelToFileModel";

    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    public void setProject(ProjectModel pModel);

    /**
     * Get the {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    public ProjectModel getProject();

    /**
     * Set message
     */
    @Property(PROPERTY_MESSAGE)
    public void setMessage(String message);

    /**
     * Get message
     */
    @Property(PROPERTY_MESSAGE)
    public String getMessage();

    
    
    /**
     * Set ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(PROPERTY_RULE_ID)
    public void setRuleID(String ruleID);

    /**
     * Get ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(PROPERTY_RULE_ID)
    public String getRuleID();

}