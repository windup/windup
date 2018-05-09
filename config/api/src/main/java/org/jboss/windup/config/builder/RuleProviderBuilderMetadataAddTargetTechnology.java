package org.jboss.windup.config.builder;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.TechnologyReference;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com">Marco Rizzi</a>
 */
public interface RuleProviderBuilderMetadataAddTargetTechnology
{
    /**
     * Set the {@link TechnologyReference} for this {@link AbstractRuleProvider}
     */
    RuleProviderBuilderAddDependencies addTargetTechnology(TechnologyReference technologyReference);

}
