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
public class XmlSoa5Config extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("//*[local-name()='jms-listener' and @is-gateway='true']/@name"))
                    .perform(Hint.withText("Convert JMS gateway listener to service binding").withEffort(1))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath("//*[local-name()='jms-listener' and (not(@is-gateway) or @is-gateway='false')]/@name"))
                    .perform(Hint.withText("ESB-aware listener is no longer required").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='service']/@name"))
                    .perform(Hint.withText("Migrate action processing pipeline for service").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='service']/@name"))
                    .perform(Hint.withText("Create component service for").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='service']/@name"))
                    .perform(Hint.withText("Create composite service for service").withEffort(1))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='action' and not(starts-with(@class, 'org.jboss.soa.esb.actions'))]/@class"))
                    .perform(Hint.withText("Convert action class").withEffort(1))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and not(starts-with(@class, 'org.jboss.soa.esb.actions'))]/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and not(starts-with(@class, 'org.jboss.soa.esb.actions'))]/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert action class").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" Custom action classes should be migrated to CDI Beans in SOA\n" + 
                                            "                    6. These beans\n" + 
                                            "                    can be defined as services or called directly from a Camel route.\n" + 
                                            "\n" + 
                                            "                    For additional\n" + 
                                            "                    information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/action-class-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;action class microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='actions']").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='actions']").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Classification.as("Action : create component service for action processing pipeline")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("The logic and execution flow of a service in SOA 5 is defined\n" + 
                                            "                    in an\n" + 
                                            "                    action processing pipeline. In SOA 6, this logic is contained within a\n" + 
                                            "                    service component\n" + 
                                            "                    definition and expressed using any of the available\n" + 
                                            "                    implementation types in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional\n" + 
                                            "                    information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/action-pipeline-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;action pipeline microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='service']/@name").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='service']/@name").resultMatches(".*").as("2"))
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='jms-listener']/@name").as("3")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service required for service").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" Each &lt;service&gt; definition in SOA 5 represents a service\n" + 
                                            "                    which can be\n" + 
                                            "                    called from outside the application through an ESB listner. The\n" + 
                                            "                    equivalent definition in\n" + 
                                            "                    SOA 6 is a composite service.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/service-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;service migration microsite&lt;/a&gt;.")).endIteration())
                                 .and(Iteration.over("3").perform(Hint.withText("value : jms Listener name").withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jms-listener' and @is-gateway='true']/@name").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='jms-listener' and @is-gateway='true']/@name").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service binding required for listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("This listener requires a composite service binding in\n" + 
                                            "                    SwitchYard. The\n" + 
                                            "                    configuration for a JCA or JMS binding can be found in the jms-bus\n" + 
                                            "                    definition\n" + 
                                            "                    associated with this listener.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway listener microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='hibernate-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='hibernate-bus']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in hibernate-bus")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText(" Although Camel has both hibernate and jpa components that are\n" + 
                                            "                    useful\n" + 
                                            "                    in consuming records, there isn't any support for hibernate events. To\n" + 
                                            "                    migrate this sort of bus\n" + 
                                            "                    to SwitchYard you may have to build a custom\n" + 
                                            "                    SwitchYard component using the hibernate listeners, or\n" + 
                                            "                    redesign your\n" + 
                                            "                    requirements to leverage the existing Camel components available (hibernate/jpa/sql)\n" + 
                                            "                    in\n" + 
                                            "                    this area.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jms-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='jms-bus']/@busid").resultMatches(".*").as("2"))
                                .and(XmlFile.matchesXpath("//*[local-name()='jms-bus']/@busid")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in jms-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" A jms-bus definition can be converted to a JMS or JCA gateway\n" + 
                                            "                    binding\n" + 
                                            "                    on a composite service in SwitchYard. If the jms-bus configuration\n" + 
                                            "                    is used for a non-gateway\n" + 
                                            "                    listener, it does not need to be migrated to\n" + 
                                            "                    SOA 6. For additional information and tips, see the\n" + 
                                            "                    &lt;a href=\"https://github.com/windup/soa-migration/blob/master/advice/jms-bus-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;jms-bus migration microsite&lt;/a&gt;.")).endIteration())
                                            
                                .and(Iteration.over("3").perform(Hint.withText("Value : Composite-service-name").withEffort(1)).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='ftp-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='ftp-bus']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in ftp-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("A ftp-bus definition can be converted to a FTP gateway\n" + 
                                            "                    binding\n" + 
                                            "                    on a composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/ftp-bus-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;ftp-bus migration microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='camel-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='camel-bus']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in ftp-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("A camel-bus definition can be converted to a Camel gateway\n" + 
                                            "                    binding\n" + 
                                            "                    on a composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/camel-bus-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;camel-bus migration microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='udp-listener']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='udp-listener']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in udp-listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("A udp-listener definition can be converted to a TCP/UDP\n" + 
                                            "                    gateway binding\n" + 
                                            "                    on a composite service in SwitchYard.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='jms-jca-provider']/@busidref").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='jms-jca-provider']/@busidref").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service binding required for listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("A jms-jca-provider definition can be converted to a JCA\n" + 
                                            "                    gateway binding\n" + 
                                            "                    on a composite service in SwitchYard.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='http-provider']/@busidref").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='http-provider']/@busidref").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service binding required for listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText(" A http-provider definition can be converted to a HTTP gateway\n" + 
                                            "                    binding\n" + 
                                            "                    on a composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway listener microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='cron-schedule']/@scheduleid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='cron-schedule']/@scheduleid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in ftp-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" A cron-schedule definition can be converted to a Quartz\n" + 
                                            "                    gateway binding\n" + 
                                            "                    on a composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/cron-schedule-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;cron-schedule migration microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='ftp-listener' and @is-gateway='true']/@name").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='ftp-listener' and @is-gateway='true']/@name").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service binding required for listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("This listener requires a composite service binding in\n" + 
                                            "                    SwitchYard. The\n" + 
                                            "                    configuration for a FTP binding can be found in the ftp-bus\n" + 
                                            "                    definition associated with\n" + 
                                            "                    this listener.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway listener microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='camel-gateway']/@busidref").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='camel-gateway']/@busidref").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : composite service binding required for listener")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("This gateway requires a composite service binding in\n" + 
                                            "                    SwitchYard. The\n" + 
                                            "                    configuration for a Camel binding can be found in the camel-bus\n" + 
                                            "                    definition associated\n" + 
                                            "                    with this listener.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway listener microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='http-gateway']/@name").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='http-gateway']/@name").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace with HTTP binding").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" A &lt;http-gateway&gt; can be replaced in SwitchYard by a\n" + 
                                            "                    http\n" + 
                                            "                    binding added to your composite service.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/http-gateway-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;http-gateway microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.smooks.SmooksAction']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.smooks.SmooksAction']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SmooksAction to Transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("SwitchYard uses a &lt;transform&gt; to replace the invocation\n" + 
                                            "                    of as SmooksAction\n" + 
                                            "                    to transform message content. You most likely will want to use a Smooks transform\n" + 
                                            "                    to specify your Smooks configuration and from/to types.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//property[@name='smooksConfig']/@name").as("1")
                                .and(XmlFile.from("1").matchesXpath("//property[@name='smooksConfig']/@name").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : Smooks config conversion").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In a SwitchYard Smooks transform, you can specify your Smooks\n" + 
                                            "                    configuration with the \"config\" attribute.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.smooks.SmooksAction']/*[local-name()='property' and @name='resultType']")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.smooks.SmooksAction']/*[local-name()='property' and @name='resultType']").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SmooksAction to Transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("SwitchYard uses a &lt;transform&gt; to replace the invocation\n" + 
                                            "                    of as SmooksAction\n" + 
                                            "                    to transform message content. You most likely will want to use a Smooks transform\n" + 
                                            "                    to specify your Smooks configuration and from/to types.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.proxy.SOAPProxy']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.proxy.SOAPProxy']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SOAPProxy").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("Instead of the JBoss ESB SOAPProxy action which transforms a\n" + 
                                            "                    specified\n" + 
                                            "                    WSDL and rewrites the address to the JBossESB server, SwitchYard\n" + 
                                            "                    relies on Camel's routing\n" + 
                                            "                    capability to forward\n" + 
                                            "                    requests from a proxying service to the source. Create a proxy\n" + 
                                            "                    service and a\n" + 
                                            "                    reference to the original service, and then use Camel\n" + 
                                            "                    to route them.\n" + 
                                            "\n" + 
                                            "                    For additional information and\n" + 
                                            "                    tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/soap-proxy.md\"\n" + 
                                            "                    target=\"_blank\"&gt;SOAPProxy microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.SystemPrintln']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.SystemPrintln']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SystemPrintln").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to log your message (or a static logging message),\n" + 
                                            "                    you\n" + 
                                            "                    may want to create a Bean service which logs the message in the\n" + 
                                            "                    manner you wish, or you can use\n" + 
                                            "                    Camel routing to log static\n" + 
                                            "                    logging messages.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/action-class-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;action class microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert StaticRouter to Camel routing").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to configure static routes for your message in\n" + 
                                            "                    SwitchYard, you\n" + 
                                            "                    should use Camel's routing (either through Java DSL routes or route.xml).\n" + 
                                            "\n" + 
                                            "                    For\n" + 
                                            "                    additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/action-class-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;action class microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.JMSRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.JMSRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert JMSRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the JMSRouter in SwitchYard,\n" + 
                                            "                    you should use a JMS binding.\n" + 
                                            "                    You may need to review the options for JMS bindings in SwitchYard if\n" + 
                                            "                    you are using the\n" + 
                                            "                    unwrap property.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/action-class-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;action class microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.TestMessageStore']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.TestMessageStore']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : remove TestMessageStore").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("TestMessageStore is a out-of-the-box ESB action that is used\n" + 
                                            "                    in JBoss AS container tests to store a message with some form\n" + 
                                            "                    of logging - to a file, JMX, etc.\n" + 
                                            "                    TestMessageStore is used throughou\n" + 
                                            "                    the JBoss ESB sample projects to help test the results of\n" + 
                                            "                    processed messages.\n" + 
                                            "\n" + 
                                            "                    SwitchYard is able to leverage Arquillian to do container\n" + 
                                            "                    testing, so\n" + 
                                            "                    TestMessageStore is not really necessary in SwitchYard\n" + 
                                            "                    for testing.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.scripting.GroovyActionProcessor']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.scripting.GroovyActionProcessor']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace GroovyActionProcessor with Camel").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" The GroovyActionProcessor action executes a Groovy script.\n" + 
                                            "                    You can duplicate this functionality in SwitchYard through Camel\n" + 
                                            "                    routing (both Java and XML.)\n" + 
                                            "\n" + 
                                            "                    For\n" + 
                                            "                    additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://docs.jboss.org/author/display/SWITCHYARD/Camel#Camel-Scriptinglanguages\"\n" + 
                                            "                    target=\"_blank\"&gt;Camel / Scripting languages documentation&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.services.jbpm.actions.BpmProcessor']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.services.jbpm.actions.BpmProcessor']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : Replace BpmProcessor").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("The BpmProcessor makes calls to jBPM 3 through the jBPM\n" + 
                                            "                    command API.\n" + 
                                            "                    SwitchYard supports jBPM 5, so you will need to migrate your existing\n" + 
                                            "                    workflow from jBPM\n" + 
                                            "                    3 to jBPM 5 and use a SwitchYard BPM implementation.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/bpm_migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;BPM microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='fs-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='fs-bus']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in fs-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("A fs-bus definition can be converted to a Camel binding\n" + 
                                            "                    on a\n" + 
                                            "                    composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway migration microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile.matchesXpath("//*[local-name()='fs-bus']/@busid").as("1")
                                .and(XmlFile.from("1").matchesXpath("//*[local-name()='fs-bus']/@busid").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : service binding configuration in fs-bus").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" A fs-bus definition can be converted to a Camel binding\n" + 
                                            "                    on a\n" + 
                                            "                    composite service in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the\n" + 
                                            "                    &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/gateway-listener-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;gateway migration microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.Notifier']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.Notifier']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert Notifiers to bindings").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses notifiers to transform ESB aware messages to a\n" + 
                                            "                    format that ESB-unaware services can handle. SwitchYard uses\n" + 
                                            "                    bi-directional gateways to transfer\n" + 
                                            "                    messages.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ByteArrayToString']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ByteArrayToString']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert ByteArrayToString to bindings").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText(" JBoss ESB uses a ByteArrayToString action to do conversion on\n" + 
                                            "                    a message body.\n" + 
                                            "                    SwitchYard would use Camel to do type conversion.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.LongToDateConverter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.LongToDateConverter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert LongToDateConverter to a Camel type conversion")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the LongToDateConverter action to do\n" + 
                                            "                    conversion on a message body.\n" + 
                                            "                    SwitchYard would use Camel to do type conversion.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.LongToDateConverter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.LongToDateConverter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert LongToDateConverter to a Camel type conversion")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the LongToDateConverter action to do\n" + 
                                            "                    conversion on a message body.\n" + 
                                            "                    SwitchYard would use Camel to do type conversion.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectToCSVString']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectToCSVString']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert ObjectToCSVString to a Camel type conversion")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText(" JBoss ESB uses the ObjectToCSVString action to do conversion\n" + 
                                            "                    on a message body.\n" + 
                                            "                    SwitchYard would use Camel or a possibly a Smooks transform to do this sort of\n" + 
                                            "                    conversion.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectInvoke']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectInvoke']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert ObjectInvoke to a bean service").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses a ObjectInvoke action to do invoke a processor\n" + 
                                            "                    on a message.\n" + 
                                            "                    SwitchYard would use a bean component to do something similar.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectToXStream']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.ObjectToXStream']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert ObjectToXStream to a transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses a ObjectToXStream action to do convert an\n" + 
                                            "                    Object payload to XML using the XStream\n" + 
                                            "                    processor. SwitchYard would use Camel or a possibly a Smooks\n" + 
                                            "                    transform to do this sort of conversion.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.XStreamToObject']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.XStreamToObject']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert XStreamToObject to a transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses a XStreamToObject action to convert XML in a\n" + 
                                            "                    payload to an object using the\n" + 
                                            "                    XStream processor. SwitchYard would use Camel or a possibly a Smooks\n" + 
                                            "                    transform to do this sort of conversion.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.transformation.xslt.XsltAction']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.transformation.xslt.XsltAction']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert XsltAction to a transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses a XStreamToObject action to convert XML in a\n" + 
                                            "                    payload to an object using the\n" + 
                                            "                    XStream processor. SwitchYard would use Camel or a possibly a Smooks\n" + 
                                            "                    transform to do this sort of conversion.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.transformation.xslt.XsltAction']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.transformation.xslt.XsltAction']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert XsltAction to a transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the XsltAction action to transform documents\n" + 
                                            "                    in a payload. SwitchYard would use\n" + 
                                            "                    Camel or a possibly a Smooks transform to do this sort of\n" + 
                                            "                    conversion.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.SmooksTransformer']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.converters.SmooksTransformer']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SmooksTransformer to Transform").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("SwitchYard uses a &lt;transform&gt; to replace the invocation\n" + 
                                            "                    of a SmooksTransformer\n" + 
                                            "                    to transform message content. You most likely will want to use a Smooks\n" + 
                                            "                    transform\n" + 
                                            "                    to specify your Smooks configuration and from/to types.\n" + 
                                            "\n" + 
                                            "                    For additional information and tips,\n" + 
                                            "                    see the &lt;a\n" + 
                                            "                    href=\"https://github.com/windup/soa-migration/blob/master/advice/transformation-migration.md\"\n" + 
                                            "                    target=\"_blank\"&gt;transformation microsite&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.MessagePersister']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.MessagePersister']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert MessagePersister to SQL binding").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("SwitchYard uses the MessagePersister action to persist a\n" + 
                                            "                    message. SwitchYard would uses\n" + 
                                            "                    a SQL reference binding to accomplish something similar.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.EJBProcessor']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.EJBProcessor']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace EJBProcessor with a bean service").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the EJBProcessor action to invoke a stateless\n" + 
                                            "                    session bean with the\n" + 
                                            "                    contents of a message. Similar things can be achieved in SwitchYard through the\n" + 
                                            "                    use of\n" + 
                                            "                    a bean service.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.scripting.ScriptingAction']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.scripting.ScriptingAction']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace ScriptingAction with Camel").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("The ScriptingAction executes a script using the\n" + 
                                            "                    BeanScriptingFramework.\n" + 
                                            "                    You can duplicate this functionality in SwitchYard through Camel\n" + 
                                            "                    routing (both\n" + 
                                            "                    Java and XML.)\n" + 
                                            "\n" + 
                                            "                    For additional information and tips, see the &lt;a\n" + 
                                            "                    href=\"https://docs.jboss.org/author/display/SWITCHYARD/Camel#Camel-Scriptinglanguages\"\n" + 
                                            "                    target=\"_blank\"&gt;Camel / Scripting languages documentation&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.Aggregator']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.Aggregator']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace Aggregator with Camel Aggregator").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the Aggregator action to aggregate a message\n" + 
                                            "                    sequence into a single\n" + 
                                            "                    aggregated message. SwitchYard would make use of Camel routing and Camel's\n" + 
                                            "                    aggregator to accomplish this.\n" + 
                                            "\n" + 
                                            "                    For more information, see &lt;a\n" + 
                                            "                    href=\"http://camel.apache.org/aggregator.html\"\n" + 
                                            "                    target=\"_blank\"&gt;Camel Aggregator\n" + 
                                            "                    documentation&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StreamingAggregator']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StreamingAggregator']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : replace Streaming Aggregator with Camel Aggregator")
                                .withEffort(1)).endIteration().and(Iteration.over("2").perform(Hint.withText("JBoss ESB uses the StreamingAggregator action to aggregate a\n" + 
                                            "                    message sequence into a single\n" + 
                                            "                    aggregated message. SwitchYard would make use of Camel routing and\n" + 
                                            "                    Camel's\n" + 
                                            "                    aggregator to accomplish this.\n" + 
                                            "\n" + 
                                            "                    For more information, see &lt;a\n" + 
                                            "                    href=\"http://camel.apache.org/aggregator.html\"\n" + 
                                            "                    target=\"_blank\"&gt;Camel Aggregator\n" + 
                                            "                    documentation&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.http.HttpRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.http.HttpRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert HttpRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the HttpRouter in SwitchYard,\n" + 
                                            "                    you should use a http reference binding.\n" + 
                                            "                    You may need to review the options for http bindings in\n" + 
                                            "                    SwitchYard.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.email.EmailRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.routing.email.EmailRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert EmailRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the EmailRouter in SwitchYard,\n" + 
                                            "                    you should use a mail reference binding.\n" + 
                                            "                    You may need to review the options for http bindings in\n" + 
                                            "                    SwitchYard.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.ContentBasedRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.ContentBasedRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert ContentBasedRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the ContentBasedRouter in\n" + 
                                            "                    SwitchYard, you should use Camel to route\n" + 
                                            "                    messages.\n" + 
                                            "\n" + 
                                            "                    A good example of this functionality can be found\n" + 
                                            "                    in the quickstarts project in the \"rules-camel-jbr\"\n" + 
                                            "                    quickstart.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticWiretap']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticWiretap']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert StaticWiretap").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the StaticWiretap in\n" + 
                                            "                    SwitchYard, you should use Camel to route\n" + 
                                            "                    messages.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert StaticRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the StaticRouter in\n" + 
                                            "                    SwitchYard, you should use Camel to route\n" + 
                                            "                    messages.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.StaticRouter']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert StaticRouter").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the StaticRouter in\n" + 
                                            "                    SwitchYard, you should use Camel to route\n" + 
                                            "                    messages.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.SOAPProcessor']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath( "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.SOAPProcessor']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SOAPProcessor").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the StaticRouter in\n" + 
                                            "                    SwitchYard, you should use a SOAP reference\n" + 
                                            "                    binding in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For more information, see &lt;a\n" + 
                                            "                    href=\"https://community.jboss.org/wiki/InvokingExternalWebServiceFromSwitchYard\"\n" + 
                                            "                    target=\"_blank\"&gt;Invoking an external Web Service from SwitchYard&lt;/a&gt;.")).endIteration()))
                    .addRule()
                    .when(XmlFile
                                .matchesXpath(
                                            "//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.SOAPClient']/@class")
                                .as("1").and(XmlFile.from("1").matchesXpath("//*[local-name()='action' and @class='org.jboss.soa.esb.actions.soap.SOAPClient']/@class").resultMatches(".*").as("2")))
                    .perform(Iteration.over("1").perform(Hint.withText("Action : convert SOAPClient").withEffort(1)).endIteration()
                                .and(Iteration.over("2").perform(Hint.withText("In order to replace the use of the SOAPClient in SwitchYard,\n" + 
                                            "                    you should use a SOAP reference\n" + 
                                            "                    binding in SwitchYard.\n" + 
                                            "\n" + 
                                            "                    For more information, see &lt;a\n" + 
                                            "                    href=\"https://community.jboss.org/wiki/InvokingExternalWebServiceFromSwitchYard\"\n" + 
                                            "                    target=\"_blank\"&gt;Invoking an external Web Service from SwitchYard&lt;/a&gt;.")).endIteration()));
                    /*
                     * TODO: add to java rules
                     <windup:pipeline type="JAVA" id="java-extension-decorators">
        <!-- Action class becomes bean service -->
        <windup:java-classification regex="org.jboss.soa.esb.actions.AbstractActionLifecycle$"
            description="JBossESB Action Class"/>
    </windup:pipeline>

    <windup:java-hints id="java-persistence-hints">
        <windup:java-hint source-type="INHERITANCE" regex="org.jboss.soa.esb.actions.AbstractActionLifecycle$">
            Action classes in SOA 5 are simply CDI Beans
            in SOA 6. The extension of AbstractionActionLifecycle is no longer necessary.
            An action class can become a
            standalone service in SOA 6 or it can be invoked as a bean from a Camel route.

            For additional information and
            tips, see the &lt;a
            href="https://github.com/windup/soa-migration/blob/master/advice/action-class-migration.md"
            target="_blank"&gt;action class microsite&lt;/a&gt;.
        </windup:java-hint>
        <windup:java-hint regex="org.jboss.soa.esb.message.Message" effort="1">
            Access to the SwitchYard
            Message instance is no longer required in SOA 6 to access message content for a service
            invocation.
            If you are
            converting this action class to a CDI bean to be invoked from Camel, you can use the
            org.apache.camel.Message
            interface in place of org.jboss.soa.esb.message.Message.

            For additional information
            and tips, see the &lt;a
            href="https://github.com/windup/soa-migration/blob/master/advice/message-access.md"
            target="_blank"&gt;message access microsite&lt;/a&gt;.
        </windup:java-hint>
        <windup:java-hint regex="org.jboss.soa.esb.helpers.ConfigTree" effort="1">
            ConfigTree is no longer
            used in SOA 6. If you have an action class that requires external configuration, convert the
            action class to
            a Bean Service in SOA 6 and use component properties to configure your Bean Service implementation.
        </windup:java-hint>
        <windup:java-hint regex="message.getBody().get()">

        </windup:java-hint>
    </windup:java-hints>*/
        return configuration;
    }
    // @formatter:on
}