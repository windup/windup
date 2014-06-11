package org.jboss.windup.rules.apps.javascanner.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupConfigurationProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AddClassFileMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileResourceModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class IndexClassFilesConfigurationProvider extends WindupConfigurationProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupConfigurationProvider>> getClassDependencies()
    {
        return generateDependencies(UnzipArchivesToTempConfigurationProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(
                GraphSearchConditionBuilder.create("classFiles")
                    .ofType(FileResourceModel.class)
                    .withProperty(FileResourceModel.PROPERTY_IS_DIRECTORY, false)
                    .withProperty(FileResourceModel.PROPERTY_FILE_PATH,
                        GraphSearchPropertyComparisonType.REGEX, ".*\\.class")
            ).perform(
                Iteration.over("classFiles").var("classFile")
                    .perform(
                        new AddClassFileMetadata("classFile")
                    ).endIteration()
            );
    }
}
