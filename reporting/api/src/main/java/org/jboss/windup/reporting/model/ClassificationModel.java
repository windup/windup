package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.association.LinkableModel;
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
public interface ClassificationModel extends EffortReportModel, LinkableModel, TaggableModel
{
    String TYPE = "ClassificationModel";
    String TYPE_PREFIX = TYPE + ":";
    String RULE_ID = TYPE_PREFIX + "ruleID";
    String CLASSIFICATION = TYPE_PREFIX + "classification";
    String DESCRIPTION = TYPE_PREFIX + "description";

    String FILE_MODEL = TYPE_PREFIX + "classificationModelToFileModel";

    /**
     * Add a {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    void addFileModel(FileModel fileModel);

    /**
     * Get the {@link FileModel} associated with this {@link ClassificationModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    Iterable<FileModel> getFileModels();

    /**
     * Set text of this {@link ClassificationModel}.
     */
    @Indexed
    @Property(CLASSIFICATION)
    void setClassification(String classification);

    /**
     * Get text of this {@link ClassificationModel}.
     */
    @Property(CLASSIFICATION)
    String getClassification();

    /**
     * Set the description text of this {@link ClassificationModel}.
     */
    @Property(DESCRIPTION)
    void setDescription(String ruleID);

    /**
     * Get the description text of this {@link ClassificationModel}.
     */
    @Property(DESCRIPTION)
    String getDescription();

    /**
     * Set ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(RULE_ID)
    void setRuleID(String ruleID);

    /**
     * Get ID of the {@link Rule} that added this {@link ClassificationModel}.
     */
    @Property(RULE_ID)
    String getRuleID();



}
