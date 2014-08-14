package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class BaseConfig extends WindupRuleProvider
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

                    /*
                     * <windup:pipeline type="JAVA" id="java-base-decorators"> <!-- CommonJ Classifiers -->
                     * <windup:java-classification source-type="INHERITANCE" regex="commonj.timers.Timer.*"
                     * description="Commonj Timer"> <windup:hints> <windup:java-hint regex="commonj.timers.Timer.*"
                     * hint="Migrate to JBoss WorkManager" effort="8" source-type="INHERITANCE"/> </windup:hints>
                     * </windup:java-classification>
                     */
                     
                    .addRule()
                    .when(
                       JavaClass.references("commonj.timers.Timer.*").at(TypeReferenceLocation.EXTENDS_TYPE)
                    )
                    .perform(
                       Iteration.over().perform(   
                          Classification.as("Commonj Timer")
                             .with(Link.to("JBoss JCA WorkManager", "https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_Operations_Network/3.1/html/Dev_Complete_Resource_Reference/JBossAS7-JBossAS7_Standalone_Server-JCA-Workmanager.html"))
                             .withEffort(0)
                          .and(Hint.withText("Migrate to JBoss JCA WorkManager").withEffort(8))
                       )
                       .endIteration()
                    )
                    
                   
                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="commonj.work.Work"
                     * description="Commonj Work" effort="2"> <windup:hints> <windup:java-hint regex="commonj.work.Work"
                     * hint="Migrate to JBoss JCA WorkManager" effort="2"/> </windup:hints>
                     * </windup:java-classification>
                     */
                     .addRule()
                     .when(
                        JavaClass.references("commonj.work.Work").at(TypeReferenceLocation.EXTENDS_TYPE)
                     )
                     .perform(
                        Iteration.over().perform(   
                           Classification.as("Commonj Work")
                              .with(Link.to("JBoss JCA WorkManager", "https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_Operations_Network/3.1/html/Dev_Complete_Resource_Reference/JBossAS7-JBossAS7_Standalone_Server-JCA-Workmanager.html"))
                              .withEffort(0)
                           .and(Hint.withText("Migrate to JBoss JCA WorkManager").withEffort(8))
                        )
                        .endIteration()
                     )
                     
                     
                     
                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="org.mule.umo.UMOFilter$"
                     * description="Mule ESB Message Filter"> <windup:decorators> <windup:link
                     * url="http://camel.apache.org/message-filter.html" description="Camel Message Filter"/>
                     * <windup:link url="http://camel.apache.org/bean-language.html"
                     * description="Camel Message Bean Filter"/> </windup:decorators> </windup:java-classification>
                     */
                     .addRule()
                     .when(
                        JavaClass.references("org.mule.umo.UMOFilter$").at(TypeReferenceLocation.EXTENDS_TYPE)
                     )
                     .perform(
                        Iteration.over().perform(   
                           Classification.of("#{ref.file}").as("Mule ESB Message Filter")
                              .with(Link.to("Camel Message Filter", "http://camel.apache.org/message-filter.html"))
                              .with(Link.to("Camel Message Bean Filter", "http://camel.apache.org/bean-language.html"))
                              .withEffort(0)
                        )
                        .endIteration()
                     )
                     
                     
                    /*
                     * <windup:java-classification regex="org.jboss.wsf.*" description="JBoss Web Services Specific">
                     * <windup:decorators> <windup:link url="https://community.jboss.org/wiki/JBossWS4MigrationGuide"
                     * description="JBoss Web Service (EAP4) Migration Guide"/> </windup:decorators>
                     * </windup:java-classification>
                     */
                     
                     .addRule()
                     .when(
                        JavaClass.references("org.jboss.wsf.*").at(TypeReferenceLocation.EXTENDS_TYPE)
                     )
                     .perform(
                        Iteration.over().perform(   
                           Classification.as("JBoss Web Services Specific")
                              .with(Link.to("JBoss Web Service (EAP4) Migration Guide", "https://community.jboss.org/wiki/JBossWS4MigrationGuide"))
                              .withEffort(0)
                        )
                        .endIteration()
                     )
                     
                    /*
                     * <windup:java-classification source-type="INHERITANCE"
                     * regex="org.mule.transformers.AbstractTransformer$" description="Mule ESB Transformer">
                     * <windup:decorators> <windup:link url="http://camel.apache.org/type-converter.html"
                     * description="Camel Converter"/> </windup:decorators> </windup:java-classification>
                     */
                     
                     .addRule()
                     .when(
                        JavaClass.references("org.mule.transformers.AbstractTransformer$").at(TypeReferenceLocation.EXTENDS_TYPE)
                     )
                     .perform(
                        Iteration.over().perform(   
                           Classification.as("Mule ESB Transformer")
                              .with(Link.to("Camel Converter", "http://camel.apache.org/type-converter.html"))
                              .withEffort(0)
                        )
                        .endIteration()
                     )
                     
                     ;

        return configuration;
    }
    // @formatter:on
}
