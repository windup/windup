package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.config.Link;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * One of the steps of Classification definition.
 * @author mbriskar
 */
public interface ClassificationDescription  extends OperationBuilder
{
    /**
     * Specify the link describing the topic more precisely.
     * @param link the {@link Link} describing the topic more precisely
     * @return next step of {@link Classification} definition to specify more links or effort
     */
    ClassificationLink with(Link link);
    
    /**
     * Specify the effort that that represents the level of effort required to fix the object. Use this only if you don't want to specify any extra link.
     * @param effort Effort needed to be put. Will be used to count story points for the whole application.
     * @return finish the Classification definition
     */
    Classification withEffort(int effort);
}
