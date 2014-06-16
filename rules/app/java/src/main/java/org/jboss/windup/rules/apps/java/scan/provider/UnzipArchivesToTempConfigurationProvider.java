package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.UnzipArchiveToTemporaryFolder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class UnzipArchivesToTempConfigurationProvider extends WindupConfigurationProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return generateDependencies(FileScannerWindupConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(GraphSearchConditionBuilder.create("inputArchives").ofType(ArchiveModel.class)
            )
            .perform(
                Iteration.over("inputArchives").var(ArchiveModel.class, "archive")
                    .perform(
                        UnzipArchiveToTemporaryFolder.unzip("archive")
                    )
                    .endIteration()
            );
    }
}
