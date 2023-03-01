package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.TechnologyReferenceModel;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.config.Link;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.List;

/**
 * This is used to classify lines within application source {@link FileModel} instances, and to provide hints and related data regarding specific
 * positions within those files.
 */
@TypeValue(InlineHintModel.TYPE)
public interface InlineHintModel extends EffortReportModel, FileLocationModel, TaggableModel, SourcesAndTargetsModel {
    String TYPE = "InlineHintModel";
    String TYPE_PREFIX = TYPE + "-";
    String TITLE = TYPE_PREFIX + "title";
    String HINT = TYPE_PREFIX + "hint";
    String RULE_ID = TYPE_PREFIX + "ruleID";
    String LINKS = TYPE_PREFIX + "links";
    String FILE_LOCATION_REFERENCE = TYPE_PREFIX + "fileLocationReference";
    String QUICKFIXES = TYPE_PREFIX + "quickfixes";
    String ISSUE_DISPLAY_MODE = "issueDisplayMode";

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
     * Contains an indicator as to which reports should display this issue. See also {@link IssueDisplayMode}.
     */
    @Property(ISSUE_DISPLAY_MODE)
    void setIssueDisplayMode(IssueDisplayMode issueDisplayMode);

    /**
     * Contains an indicator as to which reports should display this issue. See also {@link IssueDisplayMode}.
     */
    @Property(ISSUE_DISPLAY_MODE)
    IssueDisplayMode getIssueDisplayMode();

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
    List<LinkModel> getLinks();

    @Adjacency(label = QUICKFIXES, direction = Direction.OUT)
    void addQuickfix(QuickfixModel quickfixModel);

    @Adjacency(label = QUICKFIXES, direction = Direction.OUT)
    List<QuickfixModel> getQuickfixes();

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
