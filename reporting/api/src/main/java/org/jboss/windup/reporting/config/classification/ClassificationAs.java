package org.jboss.windup.reporting.config.classification;

import org.jboss.windup.reporting.config.Link;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * One of the first steps in defining the Classification
 * @author mbriskar
 *
 */
public interface ClassificationAs extends OperationBuilder
{
    /**
     * Specify the description of the classification and moves to the next step of {@link Classification} definition.
     * @param description description of the classification. If you want to specify description. specify it in this step.
     * @return next step of {@link Classification} definition
     */
    ClassificationDescription withDescription(String description);
    
    /**
     * Specify the link describing the topic more precisely. Use this method only if you don't want to specify description
     * @param link the {@link Link} describing the topic more precisely
     * @return next step of {@link Classification} definition
     */
    ClassificationLink with(Link link);
    
    /**
     * Specify the effort that that represents the level of effort required to fix the object. Use this only if you don't want to specify description nor any extra link.
     * @param effort Effort needed to be put. Will be used to count story points for the whole application.
     * @return next step of {@link Classification} definition
     */
    ClassificationEffort withEffort(int effort);
}
