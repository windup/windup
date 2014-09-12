package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlWebserviceConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("/serviceGroup/service/operation"))
                    .perform(Classification.as("Apache Axis Service Group"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/module/InFlow"))
                    .perform(Classification.as("Apache Axis Module"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/axisconfig"))
                    .perform(Classification.as("Apache Axis Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/xfire:beans").namespace("xfire",
                                "http://xfire.codehaus.org/config/1.0"))
                    .perform(Classification.as("XFire 1.x Configuration"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:handler-chains | /j2e:handler-chains")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("j2e", "http://java.sun.com/xml/ns/j2ee"))
                    .perform(Classification.as("JAX-WS Handler Chain").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/jee:webservices | /j2e:webservices")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("1")
                                .and(XmlFile.from("1").matchesXpath("//jee:handler-class | //j2e:handler-class").namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                            .namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("2"))
                                .and(XmlFile.from("1").matchesXpath("//jee:service-endpoint-interface | //j2e:service-endpoint-interface").namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                            .namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("3")))
                    .perform(Iteration.over("1").perform(Classification.as("Java Webservice Configuration").withEffort(0)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("Handler Class")).endIteration())
                                .and(Iteration.over("3").perform(Hint.withText("Service Interface")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("/cxf:extensions").namespace("cxf",
                                "http://cxf.apache.org/bus/extension"))
                    .perform(Classification.as("Apache CXF Bus Extension").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wsp:Policy").namespace("wsp",
                                "http://schemas.xmlsoap.org/ws/2004/09/policy"))
                    .perform(Classification.as("WS-Policy"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/se:Envelope").namespace("se",
                                "http://schemas.xmlsoap.org/soap/envelope/"))
                    .perform(Classification.as("SOAP Envelope").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/wsdl:definitions").namespace("wsdl",
                                "http://schemas.xmlsoap.org/wsdl/"))
                    .perform(Classification.as("WSDL Definition").withEffort(0))
                    /*   Wrong XPATH
                     * .addRule()
                    .when(XmlFile.matchesXpath("//soapenc:operation[@style='rpc'] and //*[@use='encoded']").namespace(
                                "soapenc", "http://schemas.xmlsoap.org/soap/encoding/"))
                    .perform(Classification.as("RPC-Encoded WSDL Definition").with(Link.to("CXF WSDL2Java Generator Documentation","http://cxf.apache.org/docs/maven-cxf-codegen-plugin-wsdl-to-java.html"))
                                .and(XSLTTransformation.using("transformations/xslt/wsdl-pom-plugin.xsl").withDescription("WSDL2JAVA POM Plugin - JAXB (Windup-Generated sample)")
                                            .withExtension("-sample-cxf-wsdl2java-pom-plugin.xml"))
                                .and(XSLTTransformation.using("transformations/xslt/wsdl2java-xmlbeans-pom-plugin.xsl").withDescription("WSDL2JAVA POM Plugin - XMLBeans (Windup-Generated sample)")
                                            .withExtension("-sample-cxf-wsdl2java-xmlbeans-pom-plugin.xml")))*/;

        return configuration;
    }
    // @formatter:on
}