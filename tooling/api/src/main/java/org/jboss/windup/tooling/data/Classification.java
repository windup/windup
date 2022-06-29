package org.jboss.windup.tooling.data;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * This is a non-graph dependent analogue to {@link ClassificationModel} suitable for usage after the {@link GraphContext} itself has been closed.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface Classification extends Serializable {
    /**
     * Gets the unique identifier of this classification.
     */
    Object getID();

    /**
     * This is the {@link File} that this {@link Classification} refers to.
     */
    File getFile();

    /**
     * Contains a description of this {@link Classification}. This is similar in concept to a title, and should be one sentence or less.
     */
    String getClassification();

    /**
     * Contains a description of the file and any migration steps that may be necessary.
     */
    String getDescription();

    /**
     * This contains a list of {@link Link}s for further information about the problem and its solution.
     */
    List<Link> getLinks();

    /**
     * This contains a list of {@link Quickfix}s for follow up in tools
     *
     * @return
     */
    List<Quickfix> getQuickfixes();

    /**
     * This contains the effort level as an integer (Story Points). This is based on the Scrum "modified-Fibonacci" system of effort estimation.
     */
    int getEffort();

    /**
     * This is a hint as to the severity of the problem. This may be used for supplying an icon or glyph in the report to the user.
     */
    IssueCategory getIssueCategory();

    /**
     * This contains the id of the rule that produced this {@link Classification}.
     */
    String getRuleID();
}
