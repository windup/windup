package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.resource.FileModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is used to classify lines within application source {@link FileModel} instances, and to provide hints and
 * related data regarding specific positions within those files.
 */
@TypeValue(InlineHintModel.TYPE)
public interface InlineHintModel extends FileLocationModel
{
    public static final String TYPE = "BlackListModel";
    public static final String PROPERTY_HINT = "hint";
    public static final String PROPERTY_RULE_ID = "ruleID";
    public static final String PROPERTY_EFFORT = "effort";

    public static final String FILE_MODEL = "blackListModelToFileModel";

    /**
     * Set the {@link FileModel} associated with this {@link InlineHintModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public void setFileModel(FileModel fileModel);

    /**
     * Get the {@link FileModel} associated with this {@link InlineHintModel}.
     */
    @Adjacency(label = FILE_MODEL, direction = Direction.OUT)
    public FileModel getFileModel();

    /**
     * Set the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(PROPERTY_EFFORT)
    public void setEffort(int effort);

    /**
     * Get the effort weight (E.g. How difficult is it to fix the issue?)
     */
    @Property(PROPERTY_EFFORT)
    public int getEffort();

    /**
     * Set the ID of the rule that triggered this particular blacklist entry
     */
    @Property(PROPERTY_RULE_ID)
    public void setRuleID(String ruleID);

    /**
     * Get the ID of the rule that triggered this particular blacklist entry
     */
    @Property(PROPERTY_RULE_ID)
    public String getRuleID();

    /**
     * Set the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(PROPERTY_HINT)
    public void setHint(String hint);

    /**
     * Get the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(PROPERTY_HINT)
    public String getHint();
}
