package org.jboss.windup.config.builder;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.config.phase.RulePhase;

/**
 * @author <a href="mailto:marcorizzi82@gmail.com">Marco Rizzi</a>
 */
public interface RuleProviderBuilderMetadataAddSourceTechnology
{
    /**
     * Set the {@link TechnologyReference} for this {@link AbstractRuleProvider}
     */
    RuleProviderBuilderAddDependencies addSourceTechnology(TechnologyReference technologyReference);

}
