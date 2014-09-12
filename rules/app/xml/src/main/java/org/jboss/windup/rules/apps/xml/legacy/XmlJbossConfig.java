package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlJbossConfig extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }

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
                    .when(XmlFile.matchesXpath("//mbean[@code='org.jboss.cache.TreeCache']"))
                    .perform(Classification.as("JBoss Cache").withEffort(1))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("JBoss.+DTD Java EE.+5"))
                    .perform(Classification.as("JBoss 5.x EAR Descriptor").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/classloading"))
                    .perform(Classification.as("JBoss Classloading").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jbc:classloading").namespace("jbc", "urn:jboss:classloading:1.0"))
                    .perform(Classification.as("JBoss Classloading Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/sc:components").namespace("sc",
                                "http://jboss.com/products/seam/components"))
                    .perform(Classification.as("JBoss Seam Components"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/sp:pages").namespace("sp", "http://jboss.com/products/seam/pages"))
                    .perform(Classification.as("JBoss Seam Pages"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jboss-app']").inFile("jboss-app.xml"))
                    .perform(Classification.as("JBoss EAR Configuration").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jboss-web']").inFile("jboss-web.xml"))
                    .perform(Classification.as("JBoss Web Application Descriptor").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='classloading']").inFile("jboss-classloading.xml"))
                    .perform(Classification.as("JBoss 5 Classloader Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jboss-deployment-structure']").inFile(
                                "jboss-deployment-structure.xml"))
                    .perform(Classification.as("JBoss Module and Classloading Configuration (since AS7/EAP6)")
                                .withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jbosscmp-jdbc']").inFile("jbosscmp-jdbc.xml"))
                    .perform(Classification.as("JBoss EJB2 CMP Deployment Descriptor"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jboss']").inFile("jboss.xml"))
                    .perform(Classification.as("JBoss EJB Deployment Descriptor (prior to AS7/EAP6)")
                           .and(Classification.as("If migrating to JBoss AS7 or EAP6 the &quot;jboss.xml&quot; "
                                       + "descriptor is ignored in deployments. Replace with &quot;jboss-ejb3.xml&quot;").withEffort(1)))
                                       
                    .addRule()
                    .when(XmlFile
                                .matchesXpath("//*[local-name()='security-domain' ][starts-with(., 'java:/jaas/')]/text()"))
                    .perform(Hint.withText("JBoss AS7/EAP6 Specific"))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath("//*[local-name()='security-domain' ][starts-with(., 'java:/jaas/')]/text()").resultMatches("java\\:\\/jaas\\/"))
                    .perform(Hint.withText("Remove the &quot;java:/jaas/&quot; prefix for security-domain elements in AS7/EAP6.").withEffort(1))
                    
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='policy']").inFile("login-config.xml"))
                    .perform(Classification.as("JBoss Security Configuration Descriptor (prior to AS7/EAP6)")
                                .and(Classification.as("If migrating to JBoss AS7 or EAP6 the &quot;login-config.xml&quot; descriptor is no longer supported. "
                                            + "Security is now configured in the security-domain element inside the server configuration.").withEffort(1)))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='ejb-jar']").inFile("jboss-ejb3.xml"))
                    .perform(Classification.as("JBoss EJB3 Deployment Descriptor (since AS7/EAP6)"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='webservices']").inFile("jboss-webservices.xml"))
                    .perform(Classification.as("JBoss Webservices Deployment Descriptor (since AS7/EAP6)"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/page").inFile(".+.page.xml"))
                    .perform(Classification.as("JBoss Seam Page"))
                    .addRule()
                    .when(XmlFile.matchesXpath("jboss-app").as("jboss-app").and(XmlFile.matchesXpath("jboss-app").withDTDPublicId("").as("jboss-app-no-DTD")))
                    .perform(Iteration.over("jboss-app").perform(Classification.as("Jboss App Descriptor")).endIteration()
                                .and(Iteration.over("jboss-app-no-DTD").perform(Classification.as("Jboss App XML with missing DTD detect").withEffort(1)).endIteration())
                                .and(Iteration.over("jboss-app-no-DTD").perform(XSLTTransformation.using("transformations/xslt/jboss-app-to-jboss5.xsl").withDescription("JBoss APP Descriptor - JBoss 5 (Windup-Generated)").withExtension("-jboss5.xml")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("server/mbean[@code='org.jboss.mq.server.jmx.Queue']"))
                    .perform(Classification.as("JBoss 4 JMS Configuration").withEffort(2)
                                .and(XSLTTransformation.using("transformations/xslt/jboss4-mq-to-jboss5.xsl").withDescription("Queue Destinations Service - JBoss 5 (Windup-Generated)").withExtension("-jboss5.xml")));
        return configuration;
    }
    // @formatter:on
}