package org.jboss.windup.rules.apps.xml.legacy;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 * 
 */
public class EjbConfig extends WindupRuleProvider
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
                    .when(XmlFile.matchesXpath("/j2e:ejb-jar | /jee:ejb-jar | /ejb-jar").namespace("jee", "http://java.sun.com/xml/ns/javaee").namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("ejb")
                                .and(XmlFile.from("ejb").matchesXpath("/ejb-jar//message-driven//ejb-name | /j2e:ejb-jar//j2e:message-driven//j2e:ejb-name | /jee:ejb-jar//jee:message-driven//jee:ejb-name").namespace("jee", "http://java.sun.com/xml/ns/javaee").namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("MDB"))
                                .and(XmlFile.from("ejb").matchesXpath("/ejb-jar//session//ejb-name | /j2e:ejb-jar//j2e:session//j2e:ejb-name | /jee:ejb-jar//jee:session//jee:ejb-name").namespace("jee", "http://java.sun.com/xml/ns/javaee").namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("sessionEJB"))
                                .and(XmlFile.from("ejb").matchesXpath("/ejb-jar//entity//ejb-name | /j2e:ejb-jar//j2e:entity//j2e:ejb-name | /jee:ejb-jar//jee:entity//jee:ejb-name").namespace("jee", "http://java.sun.com/xml/ns/javaee").namespace("j2e", "http://java.sun.com/xml/ns/j2ee").as("entityEJB"))
                                .and(XmlFile.from("ejb").matchesXpath("//*[local-name()='ejb-relation']/*[local-name()='ejb-relationship-role'][2]/*[local-name()='ejb-relationship-role-name']").as("ejbRelationship"))
                                .and(XmlFile.from("ejb").withDTDPublicId("Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2..").as("ejb2"))
                                .and(XmlFile.from("ejb").withDTDPublicId("Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1..").as("ejb1")))
                    .perform(Classification.of("ejb").as("EJB XML")
                             .and(Classification.of("MDB").as("EJB - MDB"))
                             .and(Hint.in("sessionEJB").withText("EJB - Session"))
                             .and(Hint.in("entityEJB").withText("EJB - Entity"))
                             .and(Hint.in("ejbRelationship").withText("EJB Relationship"))
                             .and(Classification.of("ejb1").as("EJB 1.x"))
                             .and(Classification.of("ejb2").as("EJB 2.x")));
        return configuration;
    }
    // @formatter:on
}