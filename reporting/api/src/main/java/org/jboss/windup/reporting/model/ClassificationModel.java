package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;
import org.ocpsoft.rewrite.config.Rule;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovy;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This classifies files and provides general background information about a specific {@link FileModel}. (For instance,
 * an XML file may be classified as a "XYZ Configuration File".) A {@link ClassificationModel} may also contain links to
 * additional information, or auto-translated/generated/updated versions of the source file.
 */
@TypeValue(ClassificationModel.TYPE)
public interface ClassificationModel extends WindupVertexFrame
{
    static final String TYPE = "ClassificationModel";
    static final String TYPE_PREFIX = TYPE + ":";
    static final String RULE_ID = TYPE_PREFIX + "ruleID";
    static final String CLASSIFICATION = TYPE_PREFIX + "classification";
    static final String SEVERITY = TYPE_PREFIX + TYPE_PREFIX + "severity";
    static final String DESCRIPTION = TYPE_PREFIX + "description";
    static final String EFFORT = TYPE_PREFIX + "effort";
    static final String LINKS = TYPE_PREFIX + "links";

    static final String FILE_MODEL = TYPE_PREFIX + "classificationModelToFileModel";

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
     * Add a related {@link Link} to this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link ClassificationModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    Iterable<LinkModel> getLinks();

    /**
     * Set the effort associated with this {@link ClassificationModel}.
     */
    @Property(EFFORT)
    void setEffort(int effort);

    /**
     * Get the effort associated with this {@link ClassificationModel}.
     */
    @Property(EFFORT)
    int getEffort();

    /**
     * Set text of this {@link ClassificationModel}.
     */
    @Indexed
    @Property(CLASSIFICATION)
    void setClassifiation(String classification);

    /**
     * Get text of this {@link ClassificationModel}.
     */
    @Property(CLASSIFICATION)
    String getClassification();

    /**
     * Contains a severity level that may be used to indicate to the user the severity level of a problem.
     */
    @Property(SEVERITY)
    void setSeverity(Severity severity);

    /**
     * Contains a severity level that may be used to indicate to the user the severity level of a problem.
     */
    @Property(SEVERITY)
    Severity getSeverity();

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
