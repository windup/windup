package org.jboss.windup.engine.provider;

import java.util.List;

import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.addon.config.RulePhase;
import org.jboss.windup.addon.config.WindupConfigurationProvider;
import org.jboss.windup.addon.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.addon.config.operation.GraphOperation;
import org.jboss.windup.addon.config.operation.Iteration;
import org.jboss.windup.addon.config.operation.ruleelement.UnzipArchiveToTemporaryFolder;
import org.jboss.windup.addon.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.meta.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class UnzipArchivesToTempConfigurationProvider extends WindupConfigurationProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getDependencies()
    {
        return generateDependencies(FileScannerWindupConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("inputArchives")
                                .ofType(ArchiveModel.class)
                    )
                    .perform(
                                Iteration.over("inputArchives").var(FileResourceModel.class, "archive")
                                            .perform(
                                                        UnzipArchiveToTemporaryFolder.unzip("archive")
                                            ).endIteration()
                    )
                    .addRule()
                    .when(GraphSearchConditionBuilder.create("allTheThings"))
                    .perform(
                                Iteration.over("allTheThings").var(WindupVertexFrame.class, "thing")
                                            .perform(new GraphOperation()
                                            {

                                                @Override
                                                public void perform(GraphRewrite event, EvaluationContext context)
                                                {
                                                    SelectionFactory sel = SelectionFactory.instance(event);
                                                    WindupVertexFrame frame = sel.getCurrentPayload(
                                                                WindupVertexFrame.class, "thing");

                                                    System.out.println("TADADADA: " + frame.toPrettyString());

                                                }
                                            }
                                            ).endIteration()
                    );

    }
}
