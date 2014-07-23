package org.jboss.windup.rules.java;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.rules.apps.java.config.JavaClass;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class HintsClassificationsTestRuleProvider extends WindupRuleProvider
{
    private Set<TypeReferenceModel> typeReferences = new HashSet<>();

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.MIGRATION_RULES;
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        AbstractIterationOperation<TypeReferenceModel> addTypeRefToList = new AbstractIterationOperation<TypeReferenceModel>(
                    TypeReferenceModel.class, "ref")
        {
            @Override
            public void perform(GraphRewrite event,
                        EvaluationContext context,
                        TypeReferenceModel payload)
            {
                typeReferences.add(payload);
            }
        };
        
        return ConfigurationBuilder.begin()
                    
                    .addRule()
                    .when(JavaClass.references("org.jboss.forge.furnace.*").at(TypeReferenceLocation.IMPORT).as("refs"))
                    .perform(Iteration.over("refs").as("ref")
                                .perform(Classification.of("#{ref.fileModel}").as("Furnace Service")
                                            .with(Link.to("JBoss Forge", "http://forge.jboss.org")).withEffort(0)
                                        .and(Hint.in("#{ref.fileModel}").at("ref")
                                                 .withText("Furnace type references imply that the client code must be run within a Furnace container.")
                                                 .withEffort(8)
                                        .and(addTypeRefToList))
                                ).endIteration()
                    );
    }
    // @formatter:on

    public Set<TypeReferenceModel> getTypeReferences()
    {
        return typeReferences;
    }
}
