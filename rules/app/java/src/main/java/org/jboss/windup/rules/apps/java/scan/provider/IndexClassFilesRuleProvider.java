package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.graphsearch.GraphSearchConditionBuilder;
import org.jboss.windup.config.graphsearch.GraphSearchPropertyComparisonType;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.rules.apps.java.scan.operation.AddClassFileMetadata;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class IndexClassFilesRuleProvider extends WindupRuleProvider
{

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getClassDependencies()
    {
        return generateDependencies(UnzipArchivesToTempRuleProvider.class);
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder
                    .begin()

                    .addRule()
                    .when(GraphSearchConditionBuilder.create("classFiles")
                                .ofType(FileModel.class)
                                .withProperty(FileModel.PROPERTY_IS_DIRECTORY, false)
                                .withProperty(FileModel.PROPERTY_FILE_PATH, GraphSearchPropertyComparisonType.REGEX,
                                            ".*\\.class")
                    )
                    .perform(Iteration.over("classFiles").as("classFile")
                                .perform(AddClassFileMetadata.to("classFile")).endIteration()
                    );
    }
}
