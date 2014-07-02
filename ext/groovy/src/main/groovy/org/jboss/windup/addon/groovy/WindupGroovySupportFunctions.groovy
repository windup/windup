import org.jboss.windup.ext.groovy.builder.WindupRuleProviderBuilder

supportFunctions.buildWindupRule =  { id ->
    WindupRuleProviderBuilder builder = WindupRuleProviderBuilder.buildWindupRule(id);
    windupRuleProviderBuilders.add(builder);
    return builder;
}
