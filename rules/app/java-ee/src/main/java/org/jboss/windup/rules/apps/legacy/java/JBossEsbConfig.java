package org.jboss.windup.rules.apps.legacy.java;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.blacklist.JavaClassification;
import org.jboss.windup.rules.apps.java.blacklist.ASTEventEvaluatorsBufferOperation;
import org.jboss.windup.rules.apps.java.blacklist.Types;
import org.jboss.windup.rules.apps.java.scan.ast.ClassCandidateType;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
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

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
     
        List<JavaClassification> classifications = new ArrayList<JavaClassification>();
        
        classifications.add(new JavaClassification(getID(), "JBoss ESB 5 Action Handler", "org.jboss.soa.esb.helpers.ConfigTree", 0, Types.add(ClassCandidateType.METHOD))); 
        Configuration configuration = ConfigurationBuilder.begin()
            .addRule().perform(new ASTEventEvaluatorsBufferOperation().add(classifications));
        return configuration;
        
    }
}
