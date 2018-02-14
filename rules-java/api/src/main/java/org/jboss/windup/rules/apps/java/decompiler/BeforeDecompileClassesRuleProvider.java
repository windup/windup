package org.jboss.windup.rules.apps.java.decompiler;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Predecompilation scan - performance purposes.
 */
@RuleMetadata(phase = DecompilationPhase.class)
public class BeforeDecompileClassesRuleProvider extends AbstractRuleProvider
{
    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(new ClassFilePreDecompilationScan());
    }
    // @formatter:on
}
