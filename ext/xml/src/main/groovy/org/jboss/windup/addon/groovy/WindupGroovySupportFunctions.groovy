import org.jboss.windup.ext.groovy.builder.WindupConfigurationProviderBuilder

supportFunctions.buildWindupRule =  { id ->
    WindupConfigurationProviderBuilder builder = WindupConfigurationProviderBuilder.buildWindupRule(id);
    windupConfigurationProviderBuilders.add(builder);
    return builder;
}
