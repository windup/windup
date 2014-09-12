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
public class XmlWeblogicConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("/*[local-name()='weblogic-rdbms-jar']").namespace("wle",
                                "http://xmlns.oracle.com/weblogic/weblogic-ejb-jar").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[@value='delay-updates-until-end-of-tx']/text()").as("2"))
                                .and(XmlFile.from("2").withDTDPublicId("delay-updates-until-end-of-tx$").as("3")))
                    .perform(Iteration.over("1").perform(Classification.as("Weblogic Entity EJB Configuration").withEffort(3)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("Weblogic Specific Transaction Property Delay Updates")).endIteration())
                                .and(Iteration.over("3").perform(Hint.withText("In EAP6 replace with: &lt;sync-on-commit-only&gt; in jbosscmp-jdbc.xml")).endIteration())
                                .and(Iteration.over("1").perform(XSLTTransformation.using("transformations/xslt/weblogic-entity2-to-jboss.xsl").withDescription("JBoss EJB CMP Descriptor (Windup-Generated)").withExtension("-jbosscmp-jdbc.xml")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("/weblogic-ejb-jar"))
                    .perform(Classification.as("Weblogic EJB XML").withEffort(3)
                                .and(XSLTTransformation.using("transformations/xslt/weblogic-ejb-to-jboss.xsl").withDescription("JBoss EJB Descriptor (Windup-Generated)").withExtension("-jboss.xml")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl9:weblogic-ejb-jar").namespace("wl9",
                                "http://www.bea.com/ns/weblogic/90"))
                    .perform(Classification.as("Weblogic EJB XML").withEffort(3)
                                .and(XSLTTransformation.using("transformations/xslt/weblogic9-ejb-to-jboss.xsl").withDescription("JBoss EJB Descriptor (Windup-Generated)").withExtension("-jboss.xml")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl10:weblogic-ejb-jar").namespace("wl10",
                                "http://www.bea.com/ns/weblogic/10.0"))
                    .perform(Classification.as("Weblogic EJB XML").withEffort(3)
                                .and(XSLTTransformation.using("transformations/xslt/weblogic10-ejb-to-jboss.xsl").withDescription("JBoss EJB Descriptor (Windup-Generated)").withExtension("-jboss.xml")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl10:weblogic-wsee-standaloneclient").namespace("wl10",
                                "http://www.bea.com/ns/weblogic/weblogic-wsee-standaloneclient"))
                    .perform(Classification.as("Weblogic SOAP Client Mapping").withEffort(6))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:java-wsdl-mapping").namespace("jee",
                                "http://java.sun.com/xml/ns/j2ee"))
                    .perform(Classification.as("Java to WSDL Mapping").withEffort(3))
                    .addRule()
                    .when(XmlFile.matchesXpath("/*[local-name()='weblogic-application']"))
                    .perform(Classification.as("Weblogic EAR Application Descriptor").withEffort(3))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl:webservice-policy-ref | /wl9:webservice-policy-ref")
                                .namespace("wl", "http://www.bea.com/ns/weblogic/webservice-policy-ref")
                                .namespace("wl9", "http://www.bea.com/ns/weblogic/90"))
                    .perform(Classification.as("Weblogic Webservice Policy"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl:weblogic-webservices | /wl9:weblogic-webservices")
                                .namespace("wl", "http://www.bea.com/ns/weblogic/weblogic-webservices")
                                .namespace("wl9", "http://www.bea.com/ns/weblogic/90").as("1")
                                .and(XmlFile.from("1").matchesXpath("//wl:webservice-type | //wl9:webservice-type")
                                            .namespace("wl", "http://www.bea.com/ns/weblogic/weblogic-webservices")
                                            .namespace("wl9", "http://www.bea.com/ns/weblogic/90").as("2")))
                    .perform(Iteration.over("1").perform(Classification.as("Weblogic Webservice Descriptor")).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("Webservice Type")).endIteration()))
                    
                    .addRule()
                    .when(XmlFile.matchesXpath("/*[local-name()='weblogic-jms']"))
                    .perform(Classification.as("Weblogic JMS Descriptor").withEffort(1)
                                .and(XSLTTransformation.using("transformations/xslt/weblogic-jms-to-jboss-messaging.xsl")
                                            .withDescription("JBoss Messaging Queue/Topic Configuration (Windup-Generated)")
                                            .withExtension("-jms-queuetopic-service.xml")))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "/bea:weblogic-web-app | /wlso:weblogic-web-app | /wls:weblogic-web-app | /weblogic-web-app")
                                .namespace("bea", "http://www.bea.com/ns/weblogic/90")
                                .namespace("wls", "http://www.bea.com/ns/weblogic/weblogic-web-app")
                                .namespace("wlso", "http://xmlns.oracle.com/weblogic/weblogic-web-app"))
                    .perform(Classification.as("Weblogic Web Application Descriptor").withEffort(3)
                                .and(XSLTTransformation.using("transformations/xslt/weblogic-jboss5-web-xml.xsl")
                                            .withDescription("JBoss Web EAP5 (Windup-Generated)").withExtension("-jboss-web.xml")))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("BEA.+RMI Runtime DTD 1.."))
                    .perform(Classification.as("Weblogic RMI XML Version 1.x").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wsdd:type-mapping").namespace("wsdd",
                                "http://www.bea.com/servers/wls70"))
                    .perform(Classification.as("Weblogic Webservice Type Mapping"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wlw:wlw-config").namespace("wlw",
                                "http://www.bea.com/2003/03/wlw/config/"))
                    .perform(Classification.as("Weblogic Services Configuration"))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath("/project/taskdef[@classname='weblogic.ant.taskdefs.webservices.servicegen.ServiceGenTask'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.javaschema.JavaSchema'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.autotype.JavaSource2DD'] | /project/taskdef[@classname='weblogic.ant.taskdefs.webservices.clientgen.ClientGenTask']"))
                    .perform(Classification.as("Weblogic-specifc Webservice Ant Tasks").withEffort(10))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wl:annotation-manifest | /annotation-manifest").namespace("wl",
                                "http://www.bea.com/2004/03/wlw/external-config/"))
                    .perform(Classification.as("Weblogic Annotation Manifest").withEffort(8));
        return configuration;
    }
    // @formatter:on
}