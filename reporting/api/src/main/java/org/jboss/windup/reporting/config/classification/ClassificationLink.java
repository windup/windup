package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.config.Link;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Step of the classification to specify more links or effort (effort is the last option).
 * @author mbriskar
 *
 */
public interface ClassificationLink  extends OperationBuilder
{
    /**
     * Specify the link describing the topic more precisely.
     * @param link the {@link Link} describing the topic more precisely
     * @return next step of {@link Classification} definition to specify effort
     */
    ClassificationLink with(Link link);
    
    /**
     * Specify the effort that needs to be done in order to face the issue. Use this only if you don't want to specify any extra link.
     * @param effort Effort needed to be put. Will be used to count story points for the whole application.
     * @return finish the Classification definition
     */
    ClassificationEffort withEffort(int effort);
}
