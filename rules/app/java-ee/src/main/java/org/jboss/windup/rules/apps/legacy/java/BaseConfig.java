package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.Link;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
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
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {

        Configuration configuration = ConfigurationBuilder
                    .begin()
                    /*
                     * <windup:pipeline type="JAVA" id="java-base-decorators"> <!-- CommonJ Classifiers -->
                     * <windup:java-classification source-type="INHERITANCE" regex="commonj.timers.Timer.*"
                     * description="Commonj Timer"> 
                     * <windup:hints> 
                     * <windup:java-hint regex="commonj.timers.Timer.*"
                     * hint="Migrate to JBoss WorkManager" effort="8" source-type="INHERITANCE"/> 
                     * </windup:hints>
                     * </windup:java-classification>
                     */
                    .addRule().perform(JavaClassification.add(getID(), "Commonj Timer", "commonj.timers.Timer.*",0, Types.add(ClassCandidateType.EXTENDS_TYPE))
                                .add(new BlackListRegex(getID(), "commonj.timers.Timer.*","Migrate to JBoss WorkManager", 8, Types.add(ClassCandidateType.EXTENDS_TYPE)))
                                )

                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="commonj.work.Work"
                     * description="Commonj Work" effort="2"> <windup:hints> <windup:java-hint regex="commonj.work.Work"
                     * hint="Migrate to JBoss JCA WorkManager" effort="2"/> </windup:hints>
                     * </windup:java-classification>
                     */
                    .addRule().perform(JavaClassification.add(getID(), "Commonj Work", "commonj.work.Work",2, Types.add(ClassCandidateType.EXTENDS_TYPE))
                                )
                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="org.mule.umo.UMOFilter$"
                     * description="Mule ESB Message Filter"> 
                     * <windup:decorators> 
                     * <windup:link url="http://camel.apache.org/message-filter.html" description="Camel Message Filter"/>
                     * <windup:link url="http://camel.apache.org/bean-language.html"
                     * description="Camel Message Bean Filter"/> 
                     * </windup:decorators> 
                     * </windup:java-classification>
                     */
                    .addRule().perform(JavaClassification.add(getID(), "Mule ESB Message Filter","org.mule.umo.UMOFilter$",0, Types.add(ClassCandidateType.EXTENDS_TYPE), Link.to("http://camel.apache.org/message-filter.html", "Camel Message Filter"), Link.to("http://camel.apache.org/bean-language.html",
                                        "Camel Message Bean Filter"))
                                )
                    /*
                     * <windup:java-classification regex="org.jboss.wsf.*" description="JBoss Web Services Specific">
                     * <windup:decorators> 
                     * <windup:link url="https://community.jboss.org/wiki/JBossWS4MigrationGuide"
                     * description="JBoss Web Service (EAP4) Migration Guide"/> 
                     * </windup:decorators>
                     * </windup:java-classification>
                     */
                     .addRule().perform(JavaClassification.add(getID(), "JBoss Web Services Specific" , "org.jboss.wsf.*",0, null, Link.to("https://community.jboss.org/wiki/JBossWS4MigrationGuide",
                                        "JBoss Web Service (EAP4) Migration Guide"))
                                        )
                    /*
                     * <windup:java-classification source-type="INHERITANCE"
                     * regex="org.mule.transformers.AbstractTransformer$" description="Mule ESB Transformer">
                     * <windup:decorators> 
                     * <windup:link url="http://camel.apache.org/type-converter.html"
                     * description="Camel Converter"/> 
                     * </windup:decorators> 
                     * </windup:java-classification>
                     */
                    .addRule().perform(JavaClassification.add(getID(),
                                           "Mule ESB Transformer", "org.mule.transformers.AbstractTransformer$", 0,
                                           Types.add(ClassCandidateType.EXTENDS_TYPE), Link.to("http://camel.apache.org/type-converter.html",
                                        "Camel Converter"))
                                        );

        return configuration;
    }
}
