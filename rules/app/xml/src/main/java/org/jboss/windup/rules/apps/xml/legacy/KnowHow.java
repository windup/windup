package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class KnowHow extends WindupRuleProvider
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(XmlFile.matchesXpath("//datasource/@pool-name").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//datasource/@jndi-name").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//datasource/@jta").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//datasource/@spy").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//datasource/@use-ccm").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//connection-url").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//driver-class").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//new-connection-sql").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//min-pool-size").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//max-pool-size").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//prefill").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//use-strict-min").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//transaction-isolation").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//flush-strategy").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//user-name").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//password").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//background-validation").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//use-fast-fail").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//idle-timeout-minutes").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//xa-resource-timeout").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//track-statements").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//prepared-statement-cache-size").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//check-valid-connection-sql").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath("//validate-on-match").inFile(".*ds-eap6.xml"))
                    .perform(Hint.withText("Define this attribute as one parameter with CLI."))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                "//datasources/local-tx-datasource/connection-url[contains(text(),'jdbc:postgres')]")
                                .inFile(".*-ds.xml"))
                    .perform(Hint.withText("Manually port datasource settings"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                "//datasources/local-tx-datasource/connection-url[contains(text(),'jdbc:oracle')]")
                                .inFile(".*-ds.xml"))
                    .perform(Hint.withText("Manually port datasource settings"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                "//datasources/local-tx-datasource/connection-url[contains(text(),'jdbc:sqlserver')]")
                                .inFile(".*-ds.xml"))
                    .perform(Hint.withText("Manually port datasource settings"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@port").inFile("server.xml"))
                    .perform(Hint.withText("Check port number"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@protocol").inFile("server.xml"))
                    .perform(Hint.withText("Check protocol value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@maxThreads").inFile("server.xml"))
                    .perform(Hint.withText("Check maxThreads value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@connectionTimeout").inFile("server.xml"))
                    .perform(Hint.withText("Check connectionTimeout value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@emptySessionPath").inFile("server.xml"))
                    .perform(Hint.withText("Check emptySessionPath value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@enableLookups").inFile("server.xml"))
                    .perform(Hint.withText("Check enableLookups value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@redirectPort").inFile("server.xml"))
                    .perform(Hint.withText("Check redirectPort value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector/@scheme").inFile("server.xml"))
                    .perform(Hint.withText("Check scheme value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Connector[@secure='true']").inFile("server.xml"))
                    .perform(Hint.withText("Check secure value"))
                    .addRule()
                    .when(XmlFile.matchesXpath("//Server/Service/Engine/@jvmRoute").inFile("server.xml"))
                    .perform(Hint.withText("Check jvmRoute value"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//jboss/container-configurations/container-configuration[@extends='Standard Stateless SessionBean']")
                                .inFile("jboss.xml"))
                    .perform(Hint.withText("Bean-specific instance pool can be set with one line in management CLI"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//jboss/container-configurations/container-configuration[@extends='Clustered Stateless SessionBean']")
                                .inFile("jboss.xml"))
                    .perform(Hint.withText("Bean-specific instance pool can be set with one line in management CLI"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//jboss/container-configurations/container-configuration[@extends='Standard Message Driven Bean']")
                                .inFile("jboss.xml"))
                    .perform(Hint.withText("MDB's Bean-specific instance pool can be set with one line in management CLI"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//jboss/container-configurations/container-configuration[@extends='Singleton Message Driven Bean']")
                                .inFile("jboss.xml"))
                    .perform(Hint.withText("Singleton Message Driven BeanMDB's Bean-specific instance pool can be set with one line in management CLI"))
                    .addRule()
                    .when(XmlFile.matchesXpath(
                                            "//jboss/container-configurations/container-configuration[@extends='Standard Message Inflow Driven Bean']")
                                .inFile("jboss.xml"))
                    .perform(Hint.withText("Standard Message Inflow Driven Bean's Bean-specific instance pool can be set with one line in management CLI"));
        return configuration;
    }
}