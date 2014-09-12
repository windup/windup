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
public class XmlJppConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("//p:portlet-app | //portlet-app")
                                .namespace("p", "http://java.sun.com/xml/ns/portlet/portlet-app_2_0.xsd")
                                .inFile("portlet.xml"))
                    .perform(Classification.as("Portlet 2.0 Application Descriptor").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("//p:portlet-app | //portlet-app")
                                .namespace("p", "http://java.sun.com/xml/ns/portlet/portlet-app_1_0.xsd")
                                .inFile("portlet.xml"))
                    .perform(Classification.as("Portlet 1.0 Application Descriptor").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("//webui-configuration").inFile("webui-configuration.xml"))
                    .perform(Classification.as("GateIn WebUI Configuration").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//dep:deployment | //deployment")
                                .namespace("dep", "urn:jboss:bean-deployer:2.0").inFile("gatein-jboss-beans.xml"))
                    .perform(Classification.as("JBoss Bean Deployer for GateIn").withEffort(2))
                    .addRule()
                    .when(XmlFile.matchesXpath("/k10:configuration | /k12:configuration")
                                .namespace("k10", "http://www.exoplaform.org/xml/ns/kernel_1_0.xsd")
                                .namespace("k12", "http://www.exoplatform.org/xml/ns/kernel_1_2.xsd"))
                    .perform(Classification.as("GateIn eXo Kernel Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/gdg:gadgets").namespace("gdg",
                                "http://www.gatein.org/xml/ns/gadgets_1_0"))
                    .perform(Classification.as("GateIn Gadget").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/go:node-navigation").namespace("go",
                                "http://www.gatein.org/xml/ns/gatein_objects_1_2"))
                    .perform(Classification.as("GateIn Node Navigation Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/go:page-set").namespace("go",
                                "http://www.gatein.org/xml/ns/gatein_objects_1_2"))
                    .perform(Classification.as("GateIn Page Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/go:portal-config").namespace("go",
                                "http://www.gatein.org/xml/ns/gatein_objects_1_2"))
                    .perform(Classification.as("GateIn Portal Configuration").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath("/gr:gatein-resources").namespace("gr",
                                "http://www.gatein.org/xml/ns/gatein_resources_1_2"))
                    .perform(Classification.as("GateIn Resources").withEffort(0))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "/jee:faces-config/jee:application/jee:view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')] | /je:faces-config/je:application/je:view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')] | /faces-config/application/view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')]")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("je", "http://java.sun.com/JSF/Configuration")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath("/jee:faces-config/jee:application/jee:view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')] | /je:faces-config/je:application/je:view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')] | /faces-config/application/view-handler/text()[contains(., 'org.jboss.portletbridge.application.PortletViewHandler')]")
                                            .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                            .namespace("je", "http://java.sun.com/JSF/Configuration")
                                            .resultMatches("org.jboss.portletbridge.application.PortletViewHandler")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("PortletBridge View Handler")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("Remove this entry, as it's not needed by Red Hat JBoss Portal 6.x")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "/jee:faces-config/jee:application/jee:state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')] | /je:faces-config/je:application/je:state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')] | /faces-config/application/state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')]")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("je", "http://java.sun.com/JSF/Configuration")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath(
                                            "/jee:faces-config/jee:application/jee:state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')] | /je:faces-config/je:application/je:state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')] | /faces-config/application/state-manager/text()[contains(., 'org.jboss.portletbridge.application.PortletStateManager')]")
                                .namespace("jee", "http://java.sun.com/xml/ns/javaee")
                                .namespace("je", "http://java.sun.com/JSF/Configuration")
                                            .resultMatches("org.jboss.portletbridge.application.PortletStateManager")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("PortletBridge State Manager")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("Remove this entry, as it's not needed by Red Hat JBoss Portal 6.x")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("/application/module/java/text()").as("1")
                                .and(XmlFile.from("1").matchesXpath("/application/module/java/text()").resultMatches(".*jar$").as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("Portal library")
                                .withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("Move the library to the EAR's lib directory. See https://access.redhat.com/site/documentation/en-US/Red_Hat_JBoss_Portal/6.0/html/Migration_Guide/ar01s06.html")
                                            .withEffort(0)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//*[starts-with(., 'org.jboss.portal.portlet.impl.jsr168.taglib')]/text()")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath(
                                            "//*[starts-with(., 'org.jboss.portal.portlet.impl.jsr168.taglib')]/text()")
                                            .resultMatches("org.jboss.portal.portlet.impl.jsr168.taglib.*")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("PortletBridge org.jboss.portal.portlet.impl.jsr168.taglib moved")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("This package has been moved to org.gatein.pc.portlet.impl.jsr168.taglib")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//*[starts-with(., 'org.exoplatform.web.login.InitiateLoginServlet')]/text()")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath(
                                            "//*[starts-with(., 'org.exoplatform.web.login.InitiateLoginServlet')]/text()").resultMatches("org.exoplatform.web.login.InitiateLoginServlet")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("Class org.exoplatform.web.login.InitiateLoginServlet moved")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[starts-with(., 'org.exoplatform.web.login.DoLoginServlet')]/text()")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[starts-with(., 'org.exoplatform.web.login.DoLoginServlet')]/text()").resultMatches("org.exoplatform.web.login.DoLoginServlet")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("Class org.exoplatform.web.login.DoLoginServlet moved")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[starts-with(., 'org.exoplatform.web.login.ErrorLoginServlet')]/text()")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[starts-with(., 'org.exoplatform.web.login.ErrorLoginServlet')]/text()").resultMatches("org.exoplatform.web.login.ErrorLoginServlet")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("Class org.exoplatform.web.login.ErrorLoginServlet moved")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.")
                                            .withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[starts-with(., 'org.exoplatform.web.security.PortalLoginController')]/text()")
                                .as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[starts-with(., 'org.exoplatform.web.security.PortalLoginController')]/text()")
                                            .resultMatches("org.exoplatform.web.security.PortalLoginController")
                                            .as("2")))
                    .perform(Iteration.over("1").perform(Hint
                                .withText("Class org.exoplatform.web.security.PortalLoginController moved")).endIteration()
                                .and(Iteration.over("2").perform(Hint
                                            .withText("This class was removed in Red Hat JBoss Portal Platform 6. See the web.xml/login.jsp from the sample-portal quickstart for an example on how to deal with authentication/authorization on this version.")
                                            .withEffort(1)).endIteration()));
        return configuration;
    }
    // @formatter:on
}