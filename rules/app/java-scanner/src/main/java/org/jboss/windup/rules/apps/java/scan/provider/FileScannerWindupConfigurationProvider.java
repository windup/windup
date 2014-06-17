package org.jboss.windup.rules.apps.java.scan.provider;

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
import org.jboss.windup.graph.model.resource.FileModel;
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
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return generateDependencies(CreateInputFileConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()

                    .addRule()
                    .when(GraphSearchConditionBuilder.create("inputDirectories")
                                .ofType(FileModel.class)
                                .withProperty(FileModel.PROPERTY_IS_DIRECTORY, true)
                    )
                    .perform(Iteration.over("inputDirectories").var(FileModel.class, "directory")
                                .perform(RecurseDirectoryAndAddFiles.add("directory")).endIteration()
                    )

                    .addRule()
                    .when(GraphSearchConditionBuilder.create("inputFiles")
                                .ofType(FileModel.class)
                                .withProperty(FileModel.PROPERTY_IS_DIRECTORY, false)
                                .withProperty(FileModel.PROPERTY_FILE_PATH,
                                            GraphSearchPropertyComparisonType.REGEX,
                                            ZipUtil.getEndsWithZipRegularExpression())
                    )
                    .perform(Iteration.over("inputFiles").var(FileModel.class, "file")
                                .perform(AddArchiveReferenceInformation.addReferenceInformation("file")).endIteration()
                    );

    }
}
