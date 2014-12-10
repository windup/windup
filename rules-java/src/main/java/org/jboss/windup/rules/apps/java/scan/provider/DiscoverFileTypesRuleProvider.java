package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.operation.AddArchiveReferenceInformation;
import org.jboss.windup.rules.apps.java.scan.operation.RecurseDirectoryAndAddFiles;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * Recursively traverses all directories present in graph at the time of running this
 * and adds all files under them to the graph.
 */
public class DiscoverFileTypesRuleProvider extends WindupRuleProvider
{
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Core");
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()

        .addRule()
        .when(Query.find(FileModel.class)
            .withProperty(FileModel.IS_DIRECTORY, true)
        )
        .perform(Iteration.over(FileModel.class)
            .perform(new RecurseDirectoryAndAddFiles()).endIteration()
        )

        .addRule()
        .when(Query.find(FileModel.class)
            .withProperty(FileModel.IS_DIRECTORY, false)
            .withProperty(FileModel.FILE_PATH,
                QueryPropertyComparisonType.REGEX,
                ZipUtil.getEndsWithZipRegularExpression())
        )
        .perform(Iteration.over()
            .perform(new AddArchiveReferenceInformation()).endIteration()
        );
    }
    // @formatter:on
}
