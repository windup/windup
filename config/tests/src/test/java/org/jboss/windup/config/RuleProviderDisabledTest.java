package org.jboss.windup.config;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.loader.RuleLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterValueStore;
import org.ocpsoft.rewrite.param.ParameterValueStore;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class RuleProviderDisabledTest {
    @Inject
    private GraphContextFactory factory;
    @Inject
    private RuleLoader loader;
    @Inject
    private EnabledProvider enabledProvider;
    @Inject
    private DisabledProvider disabledProvider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private DefaultEvaluationContext createEvalContext(GraphRewrite event) {
        final DefaultEvaluationContext evaluationContext = new DefaultEvaluationContext();
        final DefaultParameterValueStore values = new DefaultParameterValueStore();
        evaluationContext.put(ParameterValueStore.class, values);
        return evaluationContext;
    }

    @Test
    public void testDisabledFeature() throws Exception {
        final Path folder = OperatingSystemUtils.createTempDir().toPath();
        try (final GraphContext context = factory.create(folder, true)) {
            GraphRewrite event = new GraphRewrite(context);
            DefaultEvaluationContext evaluationContext = createEvalContext(event);

            WindupConfigurationModel windupCfg = context.getFramed().addFramedVertex(WindupConfigurationModel.class);
            FileService fileModelService = new FileService(context);
            windupCfg.addInputPath(fileModelService.createByFilePath(folder.toAbsolutePath().toString()));
            windupCfg.setOnlineMode(false);

            Predicate<RuleProvider> migrationPhase = (provider) -> provider.getMetadata().getPhase() == MigrationRulesPhase.class;

            RuleLoaderContext ruleLoaderContext = new RuleLoaderContext(Collections.emptyList(), migrationPhase);
            Configuration configuration = loader.loadConfiguration(ruleLoaderContext).getConfiguration();

            RuleSubset.create(configuration).perform(event, evaluationContext);

            Assert.assertTrue("Enabled should run", enabledProvider.executed);
            Assert.assertFalse("Disabled should not run", disabledProvider.executed);
        }
    }

    @Singleton
    @RuleMetadata
    public static class EnabledProvider extends AbstractRuleProvider {
        private boolean executed;

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }

    @Singleton
    @RuleMetadata(disabled = true)
    public static class DisabledProvider extends AbstractRuleProvider {
        private boolean executed;

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .perform(
                            new GraphOperation() {
                                @Override
                                public void perform(GraphRewrite event, EvaluationContext context) {
                                    executed = true;
                                }
                            }
                    );
        }
    }
}
