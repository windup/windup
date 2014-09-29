package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.operation.xslt.XSLTTransformation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class XmlWebsphereConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("/rl:RuleSet").namespace("rl",
                                "http://www.ibm.com/xmlns/prod/websphere/wbi/br/6.0.0"))
                    .perform(Classification.as("IBM Process Server Rules 6.0")
                                .and(XSLTTransformation.using("transformations/xslt/websphere-psrules-to-drools.xsl")
                                            .withDescription("Drools (Windup-Generated)")
                                            .withExtension("-drools-example.drl")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/applicationbnd:ApplicationBinding").namespace("applicationbnd",
                                "applicationbnd.xmi"))
                    .perform(Classification.as("Websphere EAR Application Binding"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/webappext:WebAppExtension").namespace("webappext", "webappext.xmi"))
                    .perform(Classification.as("Websphere Web Application Extension"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/ext:web-ext").namespace("ext",
                                "http://websphere.ibm.com/xml/ns/javaee"))
                    .perform(Classification.as("Websphere Web Extension").withEffort(3)
                                .and(XSLTTransformation.using("transformations/xslt/websphere-jboss5-web-xml.xsl")
                                            .withDescription("JBoss Web EAP5 (Windup-Generated)")
                                            .withExtension("-jboss-web.xml")))
                    .addRule()
                    .when(XmlFile.matchesXpath("/ext:web-bnd").namespace("ext",
                                "http://websphere.ibm.com/xml/ns/javaee"))
                    .perform(Classification.as("Websphere Web Binding Extension"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/webappbnd:WebAppBinding").namespace("webappbnd", "webappbnd.xmi"))
                    .perform(Classification.as("Websphere Web Application Binding"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/com.ibm.etools.webservice.wscext:WsClientExtension").namespace(
                                "com.ibm.etools.webservice.wscext",
                                "http://www.ibm.com/websphere/appserver/schemas/5.0.2/wscext.xmi"))
                    .perform(Classification.as("Websphere ETools WSClient Extension"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/com.ibm.etools.webservice.wscbnd:ClientBinding").namespace(
                                "com.ibm.etools.webservice.wscbnd",
                                "http://www.ibm.com/websphere/appserver/schemas/5.0.2/wscbnd.xmi"))
                    .perform(Classification.as("Websphere ETools WSClient Binding"))
                    .addRule()
                    .when(XmlFile.matchesXpath("/appdeployment:Deployment").namespace("appdeployment",
                                "http://www.ibm.com/websphere/appserver/schemas/5.0/appdeployment.xmi"))
                    .perform(Classification.as("IBM Deployment Descriptor"));
        return configuration;
    }
    // @formatter:on
}