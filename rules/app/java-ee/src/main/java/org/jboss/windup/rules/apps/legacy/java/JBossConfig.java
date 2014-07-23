package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.BlackListRegex;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.JavaScanner;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

public class JBossConfig extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
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
        List<BlackListRegex> hints = new ArrayList<BlackListRegex>();
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();

        classifications.add(new JavaClassification(getID(), "JBoss 5 JMX ManagementBean", "org.jboss.ejb3.annotation.Management", 0, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "javax.jms.QueueConnectionFactory", "If migrating from JBoss 4, replace lookup string QueueConnectionFactory with ConnectionFactory", 1, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "javax.persistence.JoinColumn", "If migrating from JBoss 4, ensure @JoinColumn is replaced with @JoinColumns({@JoinColumn", 0, Types.add(TypeReferenceLocation.TYPE)));
        hints.add(new BlackListRegex(getID(), "org.jboss.annotation.ejb.Service", "Migrated to org.jboss.ejb3.annotation.Service", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.jboss.annotation.ejb.Management", "Migrated to org.jboss.ejb3.annotation.Management", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.jboss.annotation.ejb.LocalBinding", "Migrated to org.jboss.ejb3.annotation.LocalBinding", 0, Types.add(TypeReferenceLocation.IMPORT)));
        hints.add(new BlackListRegex(getID(), "org.jboss.annotation.ejb.Depends", "Validate that JBoss 6 Dependency exists.", 0, Types.add(TypeReferenceLocation.TYPE))); 
        
        
        Configuration configuration = ConfigurationBuilder
                    .begin()
                    .addRule().perform(new JavaScanner().add(classifications).add(hints));
        return configuration;
    }
    // @formatter:on
}
