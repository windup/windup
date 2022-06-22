package org.jboss.windup.rules.apps.condition;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.phase.PostMigrationRulesPhase;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.exec.rulefilters.RuleProviderPhasePredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.rules.general.IterableFilter;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Tests the iterable-filter condition
 */
@RunWith(Arquillian.class)
public class IterableFilterTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.config:windup-config-xml"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-base"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        final AddonArchive archive = ShrinkWrap.create(AddonArchive.class)
                .addBeansXML();
        return archive;
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Inject
    private FilteredConditionRuleProvider filteredProvider;

    @Inject
    private NotFilteredConditionRuleProvider notFilteredProvider;

    @Test
    public void testFiltered() throws Exception {
        runWindupWithPredicate();
        Assert.assertEquals(6, notFilteredProvider.count);
        Assert.assertEquals(0, filteredProvider.count);

    }

    private void runWindupWithPredicate() throws Exception {
        try (GraphContext context = factory.create(true)) {
            Path inputPath = Paths.get("src/test/resources/");

            Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(), "windup_"
                    + UUID.randomUUID().toString());
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            Predicate<RuleProvider> predicate = new NotPredicate(new RuleProviderPhasePredicate(ReportGenerationPhase.class,
                    MigrationRulesPhase.class));

            WindupConfiguration windupConfiguration = new WindupConfiguration()
                    .setGraphContext(context);
            windupConfiguration.addInputPath(inputPath);
            windupConfiguration.setOutputDirectory(outputPath);
            windupConfiguration.setOptionValue(SourceModeOption.NAME, true);
            processor.execute(windupConfiguration);


        }
    }


    public abstract static class AbstractIterableFilterRuleProvider extends AbstractRuleProvider {
        public int count = 0;
        private int sizeToFilter = 0;

        public AbstractIterableFilterRuleProvider(int sizeToFilter, Class<? extends RuleProvider> currentClass) {
            super(MetadataBuilder.forProvider(currentClass)
                    .setPhase(PostMigrationRulesPhase.class));
            this.sizeToFilter = sizeToFilter;
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {

            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(IterableFilter.withSize(sizeToFilter).withWrappedCondition((FileContent) FileContent.matches("file {*}.").inFileNamed("{*}.txt")))
                    .perform(new AbstractIterationOperation<FileLocationModel>() {

                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, FileLocationModel payload) {
                            count++;
                        }
                    });
        }
        // @formatter:on
    }

    @Singleton
    public static class FilteredConditionRuleProvider extends AbstractIterableFilterRuleProvider {
        public FilteredConditionRuleProvider() {
            super(1, FilteredConditionRuleProvider.class);
        }

    }

    @Singleton
    public static class NotFilteredConditionRuleProvider extends AbstractIterableFilterRuleProvider {
        public NotFilteredConditionRuleProvider() {
            // the size of the result should be exactly 6 and therefore the iterable should not be filtered out.
            super(6, NotFilteredConditionRuleProvider.class);
        }

    }

}