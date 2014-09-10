package org.jboss.windup.rules.java;

import java.util.List;
import javax.inject.Singleton;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class TestJavaClassTestRuleProvider extends WindupRuleProvider {
    private int firstRuleMatchCount = 0;
    private int secondRuleMatchCount = 0;


    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }


    public int getFirstRuleMatchCount()
    {
        return firstRuleMatchCount;
    }


    public void setFirstRuleMatchCount(int firstRuleMatchCount)
    {
        this.firstRuleMatchCount = firstRuleMatchCount;
    }


    public int getSecondRuleMatchCount()
    {
        return secondRuleMatchCount;
    }


    public void setSecondRuleMatchCount(int secondRuleMatchCount)
    {
        this.secondRuleMatchCount = secondRuleMatchCount;
    }


    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(AnalyzeJavaFilesRuleProvider.class);
    }


    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
        .addRule().when(
            JavaClass.references("org.jboss.forge.furnace.*").inFile(".*").at(TypeReferenceLocation.IMPORT)
        ).perform(
            Iteration.over().perform(new AbstractIterationOperation<TypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                {
                    firstRuleMatchCount++;
                }
            }).endIteration()
        )
                
        .addRule().when(
            JavaClass.references("org.jboss.forge.furnace.*").inFile(".*JavaClassTest.*").at(TypeReferenceLocation.IMPORT)
        ).perform(
            Iteration.over().perform(new AbstractIterationOperation<TypeReferenceModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, TypeReferenceModel payload)
                {
                    secondRuleMatchCount++;
                }
            }).endIteration()
        );
    }
    // @formatter:on

    

}
