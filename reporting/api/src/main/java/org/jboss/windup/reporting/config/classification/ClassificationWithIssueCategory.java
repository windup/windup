package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.category.IssueCategory;
import org.jboss.windup.reporting.config.Link;

/**
 * Contains the methods that can be called after the {@link IssueCategory} has been set.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public interface ClassificationWithIssueCategory
{
    /**
     * @see ClassificationAs#with(Link)
     */
    ClassificationLink with(Link link);

    /**
     * @see ClassificationAs#withEffort(int)
     */
    ClassificationEffort withEffort(int effort);
}
