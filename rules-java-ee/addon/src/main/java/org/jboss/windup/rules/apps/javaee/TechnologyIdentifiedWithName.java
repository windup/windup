package org.jboss.windup.rules.apps.javaee;

import org.jboss.windup.config.operation.GraphOperation;
import org.ocpsoft.rewrite.config.OperationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface TechnologyIdentifiedWithName extends OperationBuilder {
    TechnologyIdentifiedWithName withTag(String tag);

    TechnologyIdentifiedWithCount numberFound(int count);
}
