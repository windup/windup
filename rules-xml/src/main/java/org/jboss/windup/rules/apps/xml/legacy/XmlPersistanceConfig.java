package org.jboss.windup.rules.apps.xml.legacy;

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
public class XmlPersistanceConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("/sqlMap "))
                    .perform(Classification.as("iBatis Mapping").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/sqlMapConfig "))
                    .perform(Classification.as("iBatis Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("persistence[not(@version)]"))
                    .perform(Classification
                                .as("JPA Configuration")
                                .withEffort(0)
                                .and(XSLTTransformation.using("transformations/xslt/jboss4-persistence-to-jboss5.xsl")
                                            .withDescription("JPA Descriptor - JBoss 5 (Windup-Generated)")
                                            .withExtension("-jboss5.xml")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jpa:persistence[@version='1.0'] | /persistence[@version='1.0']")
                                .namespace("jpa", "http://java.sun.com/xml/ns/persistence").as("JPA1"))
                    .perform(Iteration.over("JPA1").perform(
                                Classification
                                .as("JPA 1.x Configuration")
                                .withEffort(0)
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "/project/taskdef[@classname='weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.javaschema.JavaSchema'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask']")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "/project/taskdef[@classname='weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.javaschema.JavaSchema'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask']")
                                                                    .resultMatches(
                                                                                "weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask$ | weblogic.ant.taskdefs.webservices.javaschema.JavaSchema$ | weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD$ | weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Weblogic Specifc Webservice Ant Tasks")
                                                        .withEffort(10)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint.withText(
                                                                    "Replace with Apache CXF Ant Tasks or Annotations")).endIteration()))

                                            .endIteration())

                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOnASTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JOnAS Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOTMTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JOTM Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereExtendedJTATransactionLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WeblogicTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Weblogic Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())

                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.BESTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Borland ES Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JRun4TransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JRun4 AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OC4JTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("OC4J (Oracle) AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OrionTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Orion Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.ResinTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Resin Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA1")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA1"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.SunONETransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Sun ONE Application Server Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                            ).endIteration()

                    )

                    .addRule()
                    .when(XmlFile.matchesXpath("/jpa:persistence[@version='2.0'] | /persistence[@version='2.0']")
                                .namespace("jpa", "http://java.sun.com/xml/ns/persistence").as("JPA2"))
                    .perform(Iteration.over("JPA2").perform(
                                Classification
                                .as("JPA 2.x Configuration")
                                .withEffort(0)

                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "/project/taskdef[@classname='weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.javaschema.JavaSchema'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask']")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "/project/taskdef[@classname='weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.javaschema.JavaSchema'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask']")
                                                                    .resultMatches(
                                                                                "weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask$ | weblogic.ant.taskdefs.webservices.javaschema.JavaSchema$ | weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD$ | weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Weblogic Specifc Webservice Ant Tasks")
                                                        .withEffort(10)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint.withText(
                                                                    "Replace with Apache CXF Ant Tasks or Annotations")).endIteration()))

                                            .endIteration())

                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOnASTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(
                                                        Hint.withText("JOnAS Specific").withEffort(1)
                                                        ).endIteration()
                                                        .and(Iteration.over("2").perform(
                                                                    Hint.withText("Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")
                                                                    ).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOTMTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JOTM Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereExtendedJTATransactionLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WeblogicTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Weblogic Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())

                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.BESTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Borland ES Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JRun4TransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JRun4 AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OC4JTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("OC4J (Oracle) AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OrionTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Orion Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.ResinTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Resin Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("JPA2")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("JPA2"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1")
                                                                    .matchesXpath(
                                                                                "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.SunONETransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Sun ONE Application Server Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())).endIteration())

                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping"))
                    .perform(Classification.as("Hibernate Mapping"))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("hibernate-mapping-2.0"))
                    .perform(Classification.as("Hibernate 2.0 Mapping"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping/class/@outer-join").as("1")
                                .and(XmlFile.from("1").matchesXpath("/hibernate-mapping/class/@outer-join").resultMatches("outer-join").as("2")))
                    .perform(Iteration.over("1").perform(Classification
                                .as("Outer-Join Property Tag")
                                .withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(
                                            "Outer Join tag deprecated, use fetch='join'and fetch='select' instead"))
                                            .endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping/class/@unused-value").as("1")
                                .and(XmlFile.from("1").matchesXpath("/hibernate-mapping/class/@unused-value").resultMatches("unused-value").as("2")))
                    .perform(Iteration.over("1").perform(Classification
                                .as("Unused-Value Property Tag")
                                .withEffort(0)).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("Unused Value is now optional, Hibernate will set equal to 0 where sensible.")).endIteration()))
                                            
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-configuration").as("hibernate"))
                    .perform(Iteration.over("hibernate").perform(Classification
                                .as("Hibernate Configuration")).endIteration()

                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOnASTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOnASTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JOnAS Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())

                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 2
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JOTMTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JOTMTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JOTM Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 3
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereExtendedJTATransactionLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereExtendedJTATransactionLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 4
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WebSphereTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WebSphereTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WebSphereTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Websphere Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 5
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.WeblogicTransactionManagerLookup']/@value | //property[@name='transaction.manager_lookup_class' and .='org.hibernate.transaction.WeblogicTransactionManagerLookup']/text()")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.WeblogicTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Weblogic Specific")
                                                        .withEffort(1)).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 6
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.BESTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.BESTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Borland ES Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())

                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 7
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.JRun4TransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.JRun4TransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("JRun4 AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 8
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OC4JTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OC4JTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("OC4J (Oracle) AS Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 9
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.OrionTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.OrionTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Orion Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 10
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.ResinTransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.ResinTransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Resin Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))

                                            .endIteration())
                                .and(Iteration
                                            .over("hibernate")
                                            .when(XmlFile.from(Iteration.singleVariableIterationName("hibernate"))
                                                        // 11
                                                        .matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                        .as("1")
                                                        .and(XmlFile.from("1").matchesXpath(
                                                                    "//*[@value='org.hibernate.transaction.SunONETransactionManagerLookup']/@value")
                                                                    .resultMatches(
                                                                                "org.hibernate.transaction.SunONETransactionManagerLookup$")
                                                                    .as("2")))
                                            .perform(Iteration.over("1").perform(Hint
                                                        .withText("Sun ONE Application Server Specific")).endIteration()
                                                        .and(Iteration.over("2").perform(Hint
                                                                    .withText(
                                                                                "Replace with: org.hibernate.transaction.JBossTransactionManagerLookup")).endIteration()))
                                            .endIteration())

                    )
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping/class/index").as("1")
                                .and(XmlFile.from("1").matchesXpath("/hibernate-mapping/class/index").resultMatches("index").as("2")))
                    .perform(Iteration.over("1").perform(Classification
                                .as("Index Expression Semi-Deprecated")
                                .withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(
                                            "Index is now semi-deprecated. list-index and map-key preferred.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping/class/key-many-to-many").as("1")
                                .and(XmlFile.from("1").matchesXpath("/hibernate-mapping/class/key-many-to-many").resultMatches("key-many-to-many").as("2")))
                    .perform(Iteration.over("1").perform(Classification.as("Key-many-to-many tag").withEffort(1)
                                .and(Iteration.over("2").perform(Hint.withText("map-key-many-to-many preferred over key-many-to-many")).endIteration()))
                                .endIteration())
                                
                    .addRule()
                    .when(XmlFile.matchesXpath("/hibernate-mapping/class/composite-index").as("1")
                                .and(XmlFile.from("1").matchesXpath("/hibernate-mapping/class/composite-index").resultMatches("composite-index").as("2")))
                    .perform(Iteration.over("1").perform(Classification.as("Composite-index tag").withEffort(1)
                                .and(Iteration.over("2").perform(Hint.withText("composite-map-key preferred over composite-index")).endIteration())).endIteration());

        return configuration;
    }
    // @formatter:on
}