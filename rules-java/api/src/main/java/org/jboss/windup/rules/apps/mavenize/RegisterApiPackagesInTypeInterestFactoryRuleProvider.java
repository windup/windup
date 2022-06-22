package org.jboss.windup.rules.apps.mavenize;


import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.InitializationPhase;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * To actually get the queriable data about certain classes into the graph,
 * they need to be registered in {@TypeInterestFactory}.
 * <p>
 * This rule does that registration for all packages as provided by the source
 * (currently static; TODO: Take the data from WINDUP-984 - Lucene index created by nexus-repository-indexer.)
 *
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
@RuleMetadata(phase = InitializationPhase.class)
public class RegisterApiPackagesInTypeInterestFactoryRuleProvider extends AbstractRuleProvider {
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        // @formatter:off
        return ConfigurationBuilder.begin()
                .addRule().perform(new GraphOperation() {
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        final PackagesToContainingMavenArtifactsIndex packageIndex = new PackagesToContainingMavenArtifactsIndex(event.getGraphContext());
                        for (MavenCoord apiCoords : ApiDependenciesData.API_ARTIFACTS) // TODO: Get this form the index.
                        {
                            packageIndex.registerPackagesFromAPI(apiCoords);
                        }
                    }
                }).addRule().perform(new GraphOperation() {
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        final PackagesToContainingMavenArtifactsIndex packageIndex = new PackagesToContainingMavenArtifactsIndex(event.getGraphContext());
                        for (MavenCoord apiCoords : ApiDependenciesData.API_ARTIFACTS) {
                            packageIndex.markProjectsUsingPackagesFromAPI(apiCoords);
                        }
                    }
                });
        // @formatter:on
    }

}
