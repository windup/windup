package org.jboss.windup.rules.apps.java.scan.provider;

import javax.inject.Inject;

import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.phase.ArchiveMetadataExtraction;
import org.jboss.windup.config.phase.RulePhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.ConfigureArchiveTypes;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

public class DiscoverArchiveTypesRuleProvider extends WindupRuleProvider
{
    @Inject
    private GraphTypeManager graphTypeManager;

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return ArchiveMetadataExtraction.class;
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
