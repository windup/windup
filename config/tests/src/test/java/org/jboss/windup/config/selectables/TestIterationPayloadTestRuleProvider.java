package org.jboss.windup.config.selectables;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.Discovery;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class TestIterationPayloadTestRuleProvider extends WindupRuleProvider
{
    private Set<TestParentModel> parents = new HashSet<>();
    private Set<TestChildModel> children = new HashSet<>();
    private List<TestParentModel> allParents = new ArrayList<>();
    private List<TestChildModel> allChildren = new ArrayList<>();

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return Discovery.class;
    }

    public int getChildCount()
    {
        return children.size();
    }

    public int getParentCount()
    {
        return parents.size();
    }

    public int getActualChildCount()
    {
        return allChildren.size();
    }

    public int getActualParentCount()
    {
        return allParents.size();
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(Query.fromType(TestChildModel.class).as("children"))
                    .perform(Iteration
                                .over("children")
                                .as("child")
                                .perform(new AbstractIterationOperation<TestParentModel>("#{child.parent}")
                                {
                                    @Override
                                    public void perform(GraphRewrite event, EvaluationContext context,
                                                TestParentModel payload)
                                    {
                                        parents.add(payload);
                                        allParents.add(payload);
                                        TestChildModel child = (TestChildModel) resolveVariable(event, "child");
                                        children.add(child);
                                        allChildren.add(child);
                                    }
                                }).endIteration());
    }

}
