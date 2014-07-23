package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
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
                         JavaClass.references("commonj.timers.Timer.*").at(TypeReferenceLocation.EXTENDS_TYPE).as("refs")
                      )
                     .perform(
                         Iteration.over("reference").as(TypeReferenceModel.class ,"reference").perform(
                            new AbstractIterationOperation<TypeReferenceModel>(TypeReferenceModel.class, "reference")
                            {
                                public void perform(GraphRewrite event, org.ocpsoft.rewrite.context.EvaluationContext context, TypeReferenceModel reference) {
                                    
                                    GraphService<InlineHintModel> hintService = new GraphService<>(event.getGraphContext(), InlineHintModel.class);
                                    InlineHintModel hint = hintService.create();
                                    hint.setFileModel(reference.getFile());
                                    hint.setLineNumber(reference.getLineNumber());
                                    hint.setColumnNumber(reference.getColumnNumber());
                                    hint.setLength(reference.getLength());
                                    hint.setEffort(5);
                                    hint.setHint("Migrate to JBoss WorkManager");
                                };
                            }
                         )
                         .endIteration()
                     )
                     
                    .addRule()
                    .when(
                       JavaClass.references("commonj.timers.Timer.*").at(TypeReferenceLocation.EXTENDS_TYPE).as("refs")
                    )
                    .perform(
                       Iteration.over("refs").as("ref").perform(   
                          Classification.of("#{ref.fileModel}").as("Commonj Timer")
                             .with(Link.to("JBoss WorkManager", "https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_Operations_Network/3.1/html/Dev_Complete_Resource_Reference/JBossAS7-JBossAS7_Standalone_Server-JCA-Workmanager.html"))
                             .withEffort(0)
                          .and(Hint.in("#{ref.fileModel}").at("ref").withText("Migrate to JBoss WorkManager").withEffort(8))
                       )
                       .endIteration()
                    )
                    

                    .addRule()
                    .perform(
                                JavaClassification
                                            .add(getID(), "Commonj Timer", "commonj.timers.Timer.*", 0,
                                                        Types.add(TypeReferenceLocation.EXTENDS_TYPE))
                                            .add(new BlackListRegex(getID(), "commonj.timers.Timer.*",
                                                        "Migrate to JBoss WorkManager", 8, Types
                                                                    .add(TypeReferenceLocation.EXTENDS_TYPE)))
                    )

                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="commonj.work.Work"
                     * description="Commonj Work" effort="2"> <windup:hints> <windup:java-hint regex="commonj.work.Work"
                     * hint="Migrate to JBoss JCA WorkManager" effort="2"/> </windup:hints>
                     * </windup:java-classification>
                     */
                    .addRule()
                    .perform(
                                JavaClassification.add(getID(), "Commonj Work", "commonj.work.Work", 2,
                                            Types.add(TypeReferenceLocation.EXTENDS_TYPE))
                    )
                    /*
                     * <windup:java-classification source-type="INHERITANCE" regex="org.mule.umo.UMOFilter$"
                     * description="Mule ESB Message Filter"> <windup:decorators> <windup:link
                     * url="http://camel.apache.org/message-filter.html" description="Camel Message Filter"/>
                     * <windup:link url="http://camel.apache.org/bean-language.html"
                     * description="Camel Message Bean Filter"/> </windup:decorators> </windup:java-classification>
                     */
                    .addRule()
                    .perform(
                                JavaClassification.add(getID(), "Mule ESB Message Filter", "org.mule.umo.UMOFilter$",
                                            0, Types.add(TypeReferenceLocation.EXTENDS_TYPE),
                                            Link.to("Camel Message Filter",
                                                        "http://camel.apache.org/message-filter.html"),
                                            Link.to("Camel Message Bean Filter",
                                                        "http://camel.apache.org/bean-language.html"))
                    )
                    /*
                     * <windup:java-classification regex="org.jboss.wsf.*" description="JBoss Web Services Specific">
                     * <windup:decorators> <windup:link url="https://community.jboss.org/wiki/JBossWS4MigrationGuide"
                     * description="JBoss Web Service (EAP4) Migration Guide"/> </windup:decorators>
                     * </windup:java-classification>
                     */
                    .addRule()
                    .perform(JavaClassification.add(getID(), "JBoss Web Services Specific", "org.jboss.wsf.*", 0, null,
                                Link.to("JBoss Web Service (EAP4) Migration Guide",
                                            "https://community.jboss.org/wiki/JBossWS4MigrationGuide"))
                    )
                    /*
                     * <windup:java-classification source-type="INHERITANCE"
                     * regex="org.mule.transformers.AbstractTransformer$" description="Mule ESB Transformer">
                     * <windup:decorators> <windup:link url="http://camel.apache.org/type-converter.html"
                     * description="Camel Converter"/> </windup:decorators> </windup:java-classification>
                     */
                    .addRule().perform(
                                JavaClassification.add(getID(),
                                            "Mule ESB Transformer", "org.mule.transformers.AbstractTransformer$", 0,
                                            Types.add(TypeReferenceLocation.EXTENDS_TYPE),
                                            Link.to("Camel Converter", "http://camel.apache.org/type-converter.html"))
                    );

        return configuration;
    }
    // @formatter:on
}
