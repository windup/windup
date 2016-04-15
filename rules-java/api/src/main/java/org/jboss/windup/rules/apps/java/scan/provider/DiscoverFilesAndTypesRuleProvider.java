package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.phase.DiscoveryPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.apps.java.scan.operation.AddArchiveReferenceInformation;
import org.jboss.windup.rules.apps.java.scan.operation.RecurseDirectoryAndAddFiles;
import org.jboss.windup.util.ZipUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import org.jboss.windup.config.metadata.RuleMetadata;


/**
 * Recurses into directories under Windup input(s) and creates FileModel vertices for them in the graph.
 */
@RuleMetadata(phase = DiscoveryPhase.class)
public class DiscoverFilesAndTypesRuleProvider extends AbstractRuleProvider
{
    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()

        .addRule()
        .when(Query.fromType(WindupConfigurationModel.class)
            .piped(new QueryGremlinCriterion()
            {
                @Override
                public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                {
                    pipeline.out(WindupConfigurationModel.INPUT_PATH);
                    pipeline.has(FileModel.IS_DIRECTORY, true);
                }
            })
        )
        .perform(new RecurseDirectoryAndAddFiles())

        .addRule()
        .when(Query.fromType(FileModel.class)
            .withProperty(FileModel.IS_DIRECTORY, false)
            .withProperty(FileModel.FILE_PATH, QueryPropertyComparisonType.REGEX, ZipUtil.getEndsWithZipRegularExpression())
        )
        .perform(
           new AddArchiveReferenceInformation()
        );
    }
    // @formatter:on
}
