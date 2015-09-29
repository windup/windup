package org.jboss.windup.reporting.model;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.files.model.FileLocationModel;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This is used to classify lines within application source {@link FileModel} instances, and to provide hints and related data regarding specific
 * positions within those files.
 */
@TypeValue(InlineHintModel.TYPE)
public interface InlineHintModel extends EffortReportModel,FileLocationModel
{
    String TYPE = "Hint";
    String TYPE_PREFIX = TYPE + ":";
    String TITLE = TYPE_PREFIX + "title";
    String HINT = TYPE_PREFIX + "hint";
    String SEVERITY = TYPE_PREFIX + "severity";
    String RULE_ID = TYPE_PREFIX + "ruleID";
    String LINKS = TYPE_PREFIX + "links";
    String FILE_LOCATION_REFERENCE = TYPE_PREFIX + "fileLocationReference";
    String TAG = "tag";

    /**
     * A short descriptive text describing the problem covered by this hint
     */
    @Property(TITLE)
    void setTitle(String title);

    /**
     * A short descriptive text describing the problem covered by this hint
     */
    @Property(TITLE)
    String getTitle();

    /**
     * Set the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(HINT)
    void setHint(String hint);

    /**
     * Get the text to be displayed within this {@link InlineHintModel} in the designated {@link FileModel}.
     */
    @Property(HINT)
    String getHint();

    /**
     * Sets the original {@link FileLocationModel} associated with this {@link InlineHintModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    void setFileLocationReference(FileLocationModel m);

    /**
     * Gets the original{@link FileLocationModel} associated with this {@link InlineHintModel}
     */
    @Adjacency(label = FILE_LOCATION_REFERENCE, direction = Direction.OUT)
    FileLocationModel getFileLocationReference();

    /**
     * Add a related {@link Link} to this {@link InlineHintModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    void addLink(LinkModel linkDecorator);

    /**
     * Get the related {@link Link} instances associated with this {@link InlineHintModel}
     */
    @Adjacency(label = LINKS, direction = Direction.OUT)
    Iterable<LinkModel> getLinks();

    /**
     * Add a tag associated with this {@link InlineHintModel}
     */
    @SetInProperties(propertyPrefix = TAG)
    InlineHintModel addTag(String tag);

    /**
     * Set the set of tags associated with this {@link InlineHintModel}
     */
    @SetInProperties(propertyPrefix = TAG)
    InlineHintModel setTags(Set<String> tags);

    /**
     * Get the set of tags associated with this {@link InlineHintModel}
     */
    @SetInProperties(propertyPrefix = TAG)
    Set<String> getTags();

    /**
     * Set the ID of the rule that triggered this particular blacklist entry
     */
    @Property(RULE_ID)
    void setRuleID(String ruleID);

    /**
     * Get the ID of the rule that triggered this particular blacklist entry
     */
    @Property(RULE_ID)
    String getRuleID();
}
