import org.jboss.windup.config.builder.RuleProviderBuilder
import org.jboss.windup.config.metadata.RuleMetadataType;
import org.jboss.windup.ext.groovy.GroovyWindupRuleProviderLoader;
import org.jboss.forge.furnace.util.Predicate
import org.ocpsoft.rewrite.context.Context;

supportFunctions.ruleSet =  { id ->
    RuleProviderBuilder builder = RuleProviderBuilder.begin(id);
    
    /* Store in a final var so that state is maintained after script execution */
    final def script = CURRENT_WINDUP_SCRIPT;
    
    builder.setOrigin(script);
        
    windupRuleProviderBuilders.add(builder);
    return builder;
}
