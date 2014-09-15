package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlBaseConfig extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "Xml");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("/j2ee:taglib | /taglib").namespace("j2ee",
                                "http://java.sun.com/xml/ns/j2ee"))
                    .perform(Classification.as("JSP Tag Library").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId(".+XWork Validator.+"))
                    .perform(Classification.as("OpenSymphony XWork Validator").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId(".+JasperReports.+Report Design.+"))
                    .perform(Classification.as("JasperReports Report Design").withEffort(0))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath("/*[local-name()='binding']/*[local-name()='namespace'] | /*[local-name()='binding']/*[local-name()='mapping']/@value-style | /*[local-name()='binding']/*[local-name()='mapping']/*[local-name()='value']/@style | /*[local-name()='binding']/*[local-name()='mapping']/@value"))
                    .perform(Classification.as("JiBX Binding"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/MenuConfig"))
                    .perform(Classification.as("Struts Menu").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("//Apache Software Foundation//DTD Struts Configuration 1.."))
                    .perform(Classification.as("Struts 1.x").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("//Apache Software Foundation//DTD Struts Configuration 2.."))
                    .perform(Classification.as("Struts 2.x").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/struts-config"))
                    .perform(Classification.as("Struts Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/tiles-definitions | /component-definitions"))
                    .perform(Classification.as("Struts Tiles Definition").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("//catalog/chain | //catalog/chains"))
                    .perform(Classification.as("Struts Chain Of Responsibility Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/xslt:stylesheet").namespace("xslt",
                                "http://www.w3.org/1999/XSL/Transform"))
                    .perform(Classification.as("XSLT Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "/jee:facelet-taglib | /facelet-taglib | /*[local-name()='facelet-taglib']")
                                .namespace("facelet", "http://java.sun.com/JSF/Facelet")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee"))
                    .perform(Classification.as("Facelet Taglib").withEffort(2))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:faces-config | /je:faces-config | /faces-config")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("je", "http://java.sun.com/JSF/Configuration"))
                    .perform(Classification.as("JavaServer Faces Config").withEffort(0))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "/jbpm31:process-definition | /process-definition/start-state | /jbpm32:process-definition")
                                .namespace("jbpm32", "urn:jbpm.org:jpdl-3.2")
                                .namespace("jbpm31", "urn:jbpm.org:jpdl-3.1"))
                    .perform(Classification.as("jBPM 3.x Process Definition"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/oagis:catalog").namespace("oagis",
                                "urn:oasis:names:tc:entity:xmlns:xml:catalog"))
                    .perform(Classification.as("Oagis XML Catalog").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/form-validation"))
                    .perform(Classification.as("Commons Validator Rules Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/digester-rules"))
                    .perform(Classification.as("Commons Digester Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/b:netui-config | /netui-config").namespace("b",
                                "http://beehive.apache.org/netui/2004/server/config"))
                    .perform(Classification.as("Apache Beehive Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/rf:properties").namespace("rf",
                                "http://jboss.org/schema/richfaces/cdk/extensions"))
                    .perform(Classification.as("Rich Faces Properties").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:application | /j2e:application | /application")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("j2e", "http://java.sun.com/xml/ns/j2ee"))
                    .perform(Classification.as("EAR Application Descriptor").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("/web-app | /j2e:web-app | /jee:web-app")
                                .namespace("j2e", "http://java.sun.com/xml/ns/j2ee")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee"))
                    .perform(Classification.as("WAR Application Descriptor").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/dzr:mappings").namespace("dzr", "http://dozer.sourceforge.net"))
                    .perform(Classification.as("Dozer Mappings").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jam-service"))
                    .perform(Classification.as("Weblogic Webservice Annotation Descriptor"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/cbf:binding").namespace("cbf",
                                "http://www.castor.org/SourceGenerator/Binding"))
                    .perform(Classification.as("Castor Binding").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/cbf:mapping").namespace("cbf", "http://castor.exolab.org/"))
                    .perform(Classification.as("Castor Mapping").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId(".+Castor JDO Configuration.+"))
                    .perform(Classification.as("Castor JDO Config").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("Castor Mapping"))
                    .perform(Classification.as("Castor Mapping").withEffort(0))
                    .addRule()
                    .when(XmlFile.withDTDPublicId("MuleSource.+mule-configuration"))
                    .perform(Classification.as("Mule ESB Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/Configure[@class='org.mortbay.jetty.Server']"))
                    .perform(Classification.as("Jetty Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/properties/entry/@key"))
                    .perform(Classification.as("Properties Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/sitemesh"))
                    .perform(Classification.as("Sitemesh Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/rss"))
                    .perform(Classification.as("RSS File").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/axisconfig"))
                    .perform(Classification.as("Apache Axis Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                "/configuration/appender/@class | /configuration/logger | /log4j:configuration")
                                .namespace("log4j", "http://jakarta.apache.org/log4j/"))
                    .perform(Classification.as("Apache Log4j Configuration").withEffort(2))
                    .addRule()
                    .when(XmlFile.matchesXpath("/bpws:process").namespace("bpws",
                                "http://schemas.xmlsoap.org/ws/2004/03/business-process/"))
                    .perform(Classification.as("BPEL Process"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/scdl:component").namespace("scdl",
                                "http://www.ibm.com/xmlns/prod/websphere/scdl/6.0.0"))
                    .perform(Classification.as("IBM Process Server Component"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/xmi:XMI/extensionmodel:ExtensionMap")
                                .namespace("xmi", "http://www.omg.org/XMI")
                                .namespace("extensionmodel", "http:///extensionmodel.ecore"))
                    .perform(Classification.as("IBM Process Server Component"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:beans").namespace("jee", "http://java.sun.com/xml/ns/javaee"))
                    .perform(Classification.as("CDI Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/w3c:grammar").namespace("w3c", "http://www.w3.org/2001/06/grammar"))
                    .perform(Classification.as("W3C Speech Recognition Grammar"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/antlib"))
                    .perform(Classification.as("Apache Ant Task Definition").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/project/target"))
                    .perform(Classification.as("Apache Ant Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/ehcache"))
                    .perform(Classification.as("EH Cache Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/protocol_stacks/stack"))
                    .perform(Classification.as("JGroups Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/infinispan"))
                    .perform(Classification.as("Infinispan Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/cache-configs/cache-config"))
                    .perform(Classification.as("JBoss Cache Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:connector | /j2e:connector")
                                .namespace("j2e", "http://java.sun.com/xml/ns/j2ee")
                                .namespace("jee", "http://java.sun.com/xml/ns/j2ee"))
                    .perform(Classification.as("Resource Adapter").withEffort(3))
                    .addRule()
                    .when(XmlFile.matchesXpath("/module[@name='Checker'] | /checkstyle"))
                    .perform(Classification.as("Checkstyle Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/pmd:ruleset | /pmd").namespace("pmd",
                                "http://pmd.sf.net/ruleset/1.0.0"))
                    .perform(Classification.as("PMD Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/sca:composite").namespace("sca", "http://www.osoa.org/xmlns/sca/1.0").as("sca")
                                .and(XmlFile.from("sca").matchesXpath("//soapbt:binding.soap.service").namespace("soapbt","http://xsd.tns.tibco.com/amf/models/sca/binding/soap").as("tibco-soap"))
                                .and(XmlFile.from("sca").matchesXpath("//jmsbt:binding.jms.reference").namespace("jmsbt","http://xsd.tns.tibco.com/amf/models/sca/bindingtype/jms").as("tibco-jms"))
                                .and(XmlFile.from("sca").matchesXpath("//sca:property[@type='jdbc:JdbcDataSource']").namespace("sca", "http://www.osoa.org/xmlns/sca/1.0").namespace("jdbc","http://xsd.tns.tibco.com/amf/models/sharedresource/jdbc").as("dataSource"))
                                .and(XmlFile.from("sca").matchesXpath("//sca:property[@name='JMSConnectionFactory' and @type='jms:JMSConnectionFactory']").namespace("sca", "http://www.osoa.org/xmlns/sca/1.0").as("jms"))
                                .and(XmlFile.from("sca").matchesXpath("//sca:implementation.java/@class").namespace("sca", "http://www.osoa.org/xmlns/sca/1.0").as("service"))
                                )
                    .perform(Iteration.over("sca").perform(Classification.as("SCA Configuration")).endIteration()
                                .and(Iteration.over("tibco-soap").perform(Classification.as("Tibco SCA Extension: SOAP Binding")).endIteration())
                                .and(Iteration.over("tibco-jms").perform(Classification.as("Tibco SCA Extension: JMS Binding")).endIteration())
                                .and(Iteration.over("dataSource").perform(Classification.as("Data Source")).endIteration())
                                .and(Iteration.over("jms").perform(Classification.as("JMS Listener")).endIteration())
                                .and(Iteration.over("service").perform(Hint.withText("Java Service")).endIteration())
                                );
        return configuration;
    }
    // @formatter:on
}