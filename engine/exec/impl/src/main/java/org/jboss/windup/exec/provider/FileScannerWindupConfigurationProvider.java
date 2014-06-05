package org.jboss.windup.exec.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AddArchiveReferenceInformation;
import org.jboss.windup.config.operation.ruleelement.RecurseDirectoryAndAddFiles;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class FileScannerWindupConfigurationProvider extends WindupConfigurationProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getDependencies()
    {
        return generateDependencies(CreateInputFileConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(GraphSearchConditionBuilder
                                .create("inputDirectories")
                                .ofType(FileResourceModel.class)
                                .withProperty(FileResourceModel.PROPERTY_IS_DIRECTORY, true)
                    )
                    .perform(
                                Iteration.over("inputDirectories").var(FileResourceModel.class, "directory")
                                            .perform(
                                                        RecurseDirectoryAndAddFiles.add("directory")
                                            ).endIteration()
                    )
                    .addRule()
                    .when(
                                GraphSearchConditionBuilder
                                            .create("inputFiles")
                                            .ofType(FileResourceModel.class)
                                            .withProperty(FileResourceModel.PROPERTY_IS_DIRECTORY, false)
                                            .withProperty(FileResourceModel.PROPERTY_FILE_PATH,
                                                        GraphSearchPropertyComparisonType.REGEX,
                                                        ZipUtil.getEndsWithZipRegularExpression())
                    )
                    .perform(
                                Iteration.over("inputFiles")
                                            .var(FileResourceModel.class, "file")
                                            .perform(
                                                        AddArchiveReferenceInformation.addReferenceInformation("file")
                                            ).endIteration()
                    );

    }
}
