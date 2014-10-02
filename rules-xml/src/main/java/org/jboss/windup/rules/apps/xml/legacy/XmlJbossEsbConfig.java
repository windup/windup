package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlJbossEsbConfig extends WindupRuleProvider
{
    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("/jbossesb-deployment"))
                    .perform(Classification.as("JBoss ESB Deployment Descriptor").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("/*[local-name()='jbossesb']"))
                    .perform(Classification.as("JBoss ESB Pipeline Configuration").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("/*[local-name()='smooks-resource-list']"))
                    .perform(Classification.as("Smooks Configuration").withEffort(1));
        return configuration;
    }
    // @formatter:on
}