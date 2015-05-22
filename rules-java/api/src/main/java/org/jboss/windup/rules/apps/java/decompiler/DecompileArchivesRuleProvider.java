package org.jboss.windup.rules.apps.java.decompiler;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.phase.DecompilationPhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class DecompileArchivesRuleProvider extends AbstractRuleProvider
{

    public DecompileArchivesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DecompileArchivesRuleProvider.class)
                    .setPhase(DecompilationPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule()
        .perform(new ProcyonDecompilerOperation());
    }
    // @formatter:on
}