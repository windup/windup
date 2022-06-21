package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.config.Link;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ClassificationWithIssueDisplayMode {
    /**
     * @see ClassificationAs#with(Link)
     */
    ClassificationLink with(Link link);

    /**
     * @see ClassificationAs#withEffort(int)
     */
    ClassificationEffort withEffort(int effort);
}
