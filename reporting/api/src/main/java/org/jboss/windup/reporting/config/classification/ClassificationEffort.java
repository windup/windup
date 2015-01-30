package org.jboss.windup.reporting.config.classification;

import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * Interface signalizing the last step in building the {@link Classification}
 * @author mbriskar
 *
 */
public interface ClassificationEffort extends OperationBuilder
{
    // the method call 'withEffort()' should be the last method on the Classification builder pattern
}
