package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateInputFileConfigurationProvider extends WindupConfigurationProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when( GraphSearchConditionBuilder.create("inputConfigurations").ofType(WindupConfigurationModel.class) )
            .perform(
                Iteration.over("inputConfigurations")
                    .var(WindupConfigurationModel.class, "configuration")
                    .perform(
                        new AbstractIterationOperator<WindupConfigurationModel>(
                                    WindupConfigurationModel.class, "configuration")
                        {
                            @Override
                            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
                            {
                                FileModel inputPath = event.getGraphContext().getFramed()
                                            .addVertex(null, FileModel.class);
                                inputPath.setFilePath(payload.getInputPath());
                            }
                        }
                    )
                .endIteration()
            );
    }

}
