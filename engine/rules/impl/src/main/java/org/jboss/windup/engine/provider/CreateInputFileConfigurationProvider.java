package org.jboss.windup.engine.provider;

import javax.inject.Inject;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.AbstractIterationOperator;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.ApplicationReferenceDao;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class CreateInputFileConfigurationProvider extends WindupConfigurationProvider
{
    @Inject
    ApplicationReferenceDao applicationReferenceDao;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("inputConfigurations")
                                .ofType(WindupConfigurationModel.class)
                    )
                    .perform(
                                Iteration.over("inputConfigurations")
                                            .var(WindupConfigurationModel.class, "configuration")
                                            .perform(
                                                        new AbstractIterationOperator<WindupConfigurationModel>(
                                                                    WindupConfigurationModel.class, "configuration")
                                                        {
                                                            @Override
                                                            public void perform(GraphRewrite event,
                                                                        EvaluationContext context,
                                                                        WindupConfigurationModel payload)
                                                            {
                                                                FileResourceModel inputPath = event.getGraphContext()
                                                                            .getFramed()
                                                                            .addVertex(null, FileResourceModel.class);
                                                                inputPath.setFilePath(payload.getInputPath());
                                                            }
                                                        }
                                            ).endIteration()
                    );

    }

}
