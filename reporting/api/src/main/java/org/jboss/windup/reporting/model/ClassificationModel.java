package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;
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
@TypeValue(ClassificationModel.TYPE)
public interface ClassificationModel extends WindupVertexFrame
{
    public static final String TYPE = "ClassificationModel";
    public static final String RULE_ID = "ruleID";
    public static final String CLASSIFICATION = "classification";
    public static final String DESCRIPTION = "description";
    public static final String EFFORT = "effort";
    public static final String LINKS = "links";

    public static final String FILE_MODEL = "classificationModelToFileModel";

    /**
     * Add a {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public void addFileModel(FileModel fileModel);

    /**
     * Get the {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public Iterable<FileModel> getFileModels();

    /**
     * Add a related {@link Link} to this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    public void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    public Iterable<LinkModel> getLinks();

    /**
     * Set the effort associated with this {@link ClassificationModel}.
     */
    @Property(EFFORT)
    public void setEffort(int effort);

    /**
     * Get the effort associated with this {@link ClassificationModel}.
     */
    @Property(EFFORT)
    public int getEffort();

    /**
     * Set text of this {@link ClassificationModel}.
     */
    @Property(CLASSIFICATION)
    public void setClassifiation(String classification);

    /**
     * Get text of this {@link ClassificationModel}.
     */
    @Property(CLASSIFICATION)
    public String getClassification();

    /**
     * Set the description text of this {@link ClassificationModel}.
     */
    @Property(DESCRIPTION)
    public void setDescription(String ruleID);

    /**
     * Get the description text of this {@link ClassificationModel}.
     */
    @Property(DESCRIPTION)
    public String getDescription();

    /**
     * Set ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(RULE_ID)
    public void setRuleID(String ruleID);

    /**
     * Get ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(RULE_ID)
    public String getRuleID();

}
