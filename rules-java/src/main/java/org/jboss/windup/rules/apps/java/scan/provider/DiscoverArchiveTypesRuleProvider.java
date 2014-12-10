package org.jboss.windup.rules.apps.java.scan.provider;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.operation.ConfigureArchiveTypes;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;

/**
 * For all archives, recognize their type using given ConfigureArchiveTypes operation.
 * 
 * TODO: Rename to RecognizeJavaArchiveTypesRuleProvider?
 */
public class DiscoverArchiveTypesRuleProvider extends WindupRuleProvider
{
    @Inject
    private GraphTypeManager graphTypeManager;

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        super.enhanceMetadata(context);
        context.put(RuleMetadata.CATEGORY, "Java");
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(DiscoverFileTypesRuleProvider.class);
    }

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .when(
                Query.find(ArchiveModel.class)
            )
            .perform(
                Iteration.over()
                .perform(ConfigureArchiveTypes.withTypeManager(graphTypeManager))
                .endIteration()
            );
    }
    // @formatter:on
}
