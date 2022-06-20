package org.jboss.windup.rules.apps.java.scan.provider;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.ConfigureArchiveTypes;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

/**
 * Iterates over discovered archives and sets their vertices' types.
 */
@RuleMetadata(phase = ArchiveMetadataExtractionPhase.class)
public class DiscoverArchiveTypesRuleProvider extends AbstractRuleProvider {
    @Inject
    private GraphTypeManager graphTypeManager;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
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
