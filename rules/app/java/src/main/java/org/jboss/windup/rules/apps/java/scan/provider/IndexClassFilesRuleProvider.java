package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.operation.AddClassFileMetadata;
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

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()

        .addRule()
        .when(Query.find(FileModel.class)
            .withProperty(FileModel.PROPERTY_IS_DIRECTORY, false)
            .withProperty(FileModel.PROPERTY_FILE_PATH, QueryPropertyComparisonType.REGEX, ".*\\.class")
        )
        .perform(
            Iteration.over()
            .perform(new AddClassFileMetadata()).endIteration()
        );
    }
    // @formatter:on
}
