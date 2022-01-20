package org.jboss.windup.rules.apps.diva.analysis;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.ConfigurationRuleBuilderPerform;

/**
*/
@RuleMetadata(phase = MigrationRulesPhase.class)
public class DivaRuleProvider extends AbstractRuleProvider {

    // @formatter:off
   @Override
   public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
   {
       ConfigurationRuleBuilderPerform conf = ConfigurationBuilder.begin()
           .addRule()
           .perform(new DivaLauncher());
       return conf;
   }
   // @formatter:on
}
