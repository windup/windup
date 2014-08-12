package org.jboss.windup.rules.apps.legacy.java;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.context.Context;

public class JBossEsbConfig extends WindupRuleProvider
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
        /* TODO Change to use new Hints/classifications API
        
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        
        classifications.add(new JavaClassification(getID(), "JBoss ESB 5 Action Handler", "org.jboss.soa.esb.helpers.ConfigTree", 0, Types.add(TypeReferenceLocation.METHOD))); 
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new JavaScanner().add(classifications));
        return configuration;
         */
        return null;
    }
    // @formatter:on
}
