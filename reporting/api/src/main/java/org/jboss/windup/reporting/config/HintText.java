package org.jboss.windup.reporting.config;

import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.model.IssueDisplayMode;
import org.jboss.windup.reporting.quickfix.Quickfix;
import org.ocpsoft.rewrite.config.OperationBuilder;

import java.util.Set;

/**
 * One of the builder interfaces of Hint operation.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public interface HintText {

    /**
     * Provide a link describing the topic more precisely
     *
     * @param link the link to be added to the Hint
     * @return the next step in building the Hint operation
     */
    HintLink with(Link link);

    /**
     * Provide quickfix for later tooling or other processing
     *
     * @return
     */
    HintQuickfix withQuickfix(Quickfix fix);

    /**
     * Adds effort to the Hint. The effort represents the level of effort required to fix a task. Use this only if you do not want to specify the
     * link, otherwise you will specify the effort later.
     *
     * @param effort number of effort to be added to hint
     * @return the final stage of hint building
     */
    HintEffort withEffort(int effort);

    /**
     * Sets the {@link IssueDisplayMode} for this issue.
     */
    HintText withDisplayMode(IssueDisplayMode displayMode);

    /**
     * Specifies the {@link IssueCategory}.
     */
    HintWithIssueCategory withIssueCategory(IssueCategory issueCategory);

    OperationBuilder withTags(Set<String> tags);
}
