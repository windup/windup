import org.jboss.windup.config.builder.WindupRuleProviderBuilder
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.ext.groovy.GroovyWindupRuleProviderLoader;
import org.jboss.forge.furnace.util.Predicate
import org.ocpsoft.rewrite.context.Context;

supportFunctions.ruleSet =  { id ->
    WindupRuleProviderBuilder builder = WindupRuleProviderBuilder.begin(id);
    
    /* Store in a final var so that state is maintained after script execution */
    final def script = CURRENT_WINDUP_SCRIPT;
    
    builder.setMetadataEnhancer(new Predicate<Context>() {
                boolean accept(Context context) {
                    context.put(RuleMetadata.ORIGIN, script);
                };
            });
        
    windupRuleProviderBuilders.add(builder);
    return builder;
}
