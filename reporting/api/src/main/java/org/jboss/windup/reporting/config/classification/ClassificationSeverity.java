package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.Severity;

/**
 * Contains the methods that can be called after the {@link Severity} has been set.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public interface ClassificationSeverity
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
