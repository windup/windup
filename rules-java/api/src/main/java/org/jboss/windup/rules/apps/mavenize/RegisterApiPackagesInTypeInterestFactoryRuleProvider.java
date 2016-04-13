package org.jboss.windup.rules.apps.mavenize;


import java.util.logging.Logger;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.InitializationPhase;
import org.jboss.windup.graph.GraphContext;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.event.Rewrite;

/**
 * To actually get the queriable data about certain classes into the graph,
 * they need to be registered in {@TypeInterestFactory}.
 *
 * This rule does that registration for all packages as provided by the source
 * (currently static; TODO: from Lucene index created by nexus-repository-indexer.)
 */
@RuleMetadata(phase = InitializationPhase.class)
public class RegisterApiPackagesInTypeInterestFactoryRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(GraphContext grCtx)
    {
        final PackagesToContainingMavenArtifactsIndex packageIndex = new PackagesToContainingMavenArtifactsIndex(grCtx);
        // @formatter:off
        return ConfigurationBuilder.begin()
        .addRule().perform(new Operation()
        {
            public void perform(Rewrite event, EvaluationContext context)
            {
                for (MavenCoord apiCoords : ApiDependenciesData.API_ARTIFACTS) /// TODO: Get this form the index.
                {
                    packageIndex.registerPackagesFromAPI(apiCoords);
                }
            }
        }).addRule().perform(new Operation()
        {
            public void perform(Rewrite event, EvaluationContext context)
            {
                for (MavenCoord apiCoords : ApiDependenciesData.API_ARTIFACTS)
                {
                    packageIndex.markProjectsUsingPackagesFromAPI(apiCoords);
                }
            }
        });
        // @formatter:on
    }

}
