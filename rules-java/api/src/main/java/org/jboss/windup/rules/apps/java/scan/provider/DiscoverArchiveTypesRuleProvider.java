package org.jboss.windup.rules.apps.java.scan.provider;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.ConfigureArchiveTypes;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Iterates over discovered archives and sets their vertices' types.
 */
public class DiscoverArchiveTypesRuleProvider extends AbstractRuleProvider
{
    @Inject
    private GraphTypeManager graphTypeManager;

    public DiscoverArchiveTypesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverArchiveTypesRuleProvider.class)
                    .setPhase(ArchiveMetadataExtractionPhase.class));
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(
                Query.fromType(ArchiveModel.class)
            )
            .perform(
                Iteration.over()
                .perform(ConfigureArchiveTypes.withTypeManager(graphTypeManager))
                .endIteration()
            );
    }
    // @formatter:on
}
