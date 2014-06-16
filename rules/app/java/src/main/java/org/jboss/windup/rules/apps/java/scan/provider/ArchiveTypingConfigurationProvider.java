package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;
import javax.inject.Inject;
import org.jboss.forge.furnace.services.Imported;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.ConfigureArchiveTypes;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveModelPointer;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class ArchiveTypingConfigurationProvider extends WindupConfigurationProvider
{
    private @Inject Imported<ArchiveModelPointer> archiveModelPointers;

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
            .when(
                GraphSearchConditionBuilder.create("archives").ofType(ArchiveModel.class)
            )
            .perform(
                Iteration.over("archives").var("archive")
                    .perform(
                        ConfigureArchiveTypes.forVar("archive", this.archiveModelPointers)
                    ).endIteration()
            );
    }
}
