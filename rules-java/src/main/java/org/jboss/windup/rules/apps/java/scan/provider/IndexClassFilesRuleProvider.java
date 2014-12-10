package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Commit;
import org.jboss.windup.config.operation.IterationProgress;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.binary.DecompileArchivesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.operation.AddClassFileMetadata;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Discovers .class files from the applications being analyzed.
 * 
 */
public class IndexClassFilesRuleProvider extends WindupRuleProvider
{
    private static Logger LOG = Logging.get(IndexClassFilesRuleProvider.class);

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.INITIAL_ANALYSIS;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteBefore()
    {
        return asClassList(DecompileArchivesRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(FileModel.class)
                                .withProperty(FileModel.IS_DIRECTORY, false)
                                .withProperty(FileModel.FILE_PATH, QueryPropertyComparisonType.REGEX, ".*\\.class")
                    )
                    .perform(
                        new AddClassFileMetadata()
                        .and(Commit.every(10))
                        .and(IterationProgress.monitoring("Indexed class file: ", 1000))
                    );
    }
    // @formatter:on
}
