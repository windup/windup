package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.condition.UnresolvedClassCondition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindUnboundJavaReferencesRuleProvider extends AbstractRuleProvider
{
    public static final String JAVA = "java";

    public FindUnboundJavaReferencesRuleProvider()
    {
        super(MetadataBuilder.forProvider(FindUnboundJavaReferencesRuleProvider.class).addTag(JAVA));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(new UnresolvedClassCondition())
                    .perform(
                        Hint
                            .withText("This class reference could not be found on the classpath")
                            .withEffort(5)
                    );
    }
    // @formatter:on
}
