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
public class XmlJrunConfig extends WindupRuleProvider
{
    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "XML");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when( XmlFile .matchesXpath("/*[local-name()='jrun-web-app']") )
                    .perform(Classification.as("JRun Web App") .withEffort(3) )
                    .addRule()
                    .when( XmlFile .matchesXpath("/*[local-name()='jrun-ejb-jar']") )
                    .perform(Classification.as("JRun EJB") .withEffort(4) )
                    .addRule()
                    .when( XmlFile .withDTDPublicId("Macromedia, Inc.//DTD jrun-ejb-jar ...") )
                    .perform(Classification.as("JRun EJB XML") .withEffort(0) )
                    .addRule()
                    .when( XmlFile .withDTDPublicId("Macromedia, Inc.//DTD jrun-web 1..") )
                    .perform(Classification.as("JRun WAR Application Descriptor") .withEffort(0) );
        return configuration;
    }
    // @formatter:on
}