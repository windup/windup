package org.jboss.windup.rules.apps.javaee.tests;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TechnologyUsageStatisticsModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.TechnologyIdentified;
import org.jboss.windup.rules.apps.javaee.TechnologyUsageStatisticsService;
import org.jboss.windup.rules.files.condition.FileContent;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.logging.Logger;

@RunWith(Arquillian.class)
public class TechnologyIdentifiedTest {

    public static final String SHARED_LIBS_TECH = "shared-libs-tech";

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java-ee"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private static Logger LOG = Logger.getLogger(TechnologyIdentifiedTest.class.getName());

    @Inject
    TechnologyIdentifiedRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testTechnologyIdentified() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext context = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph(), true)) {
            final String inputDir = "src/test/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    TechnologyIdentifiedRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            TechnologyUsageStatisticsService service = new TechnologyUsageStatisticsService(context);
            boolean foundUsage = false;
            int numberOfModelsFound = 0;
            int totalOccurrenceCount = 0;
            for (TechnologyUsageStatisticsModel model : service.findAll()) {
                if ("test-tech".equals(model.getName())) {
                    foundUsage = true;
                    numberOfModelsFound++;
                    totalOccurrenceCount += model.getOccurrenceCount();
                }
            }

            Assert.assertTrue(foundUsage);
            Assert.assertEquals(1, numberOfModelsFound);
            Assert.assertTrue(1 < totalOccurrenceCount);
        }
    }

    @Test
    public void testDuplicateArchiveHandling() throws Exception {
        Path baseOutputPath = WindupTestUtilMethods.getTempDirectoryForGraph();
        Path graphPath = baseOutputPath.resolve("graph");
        Path reportPath = baseOutputPath.resolve("reports");

        try (GraphContext context = factory.create(graphPath, true)) {
            final String basePath = "../../test-files/duplicate/duplicate-ear-test-";
            final String[] inputPaths = new String[]{
                    basePath + "1.ear",
                    basePath + "2.ear",
                    basePath + "3.ear",
            };

            FileUtils.deleteDirectory(reportPath.toFile());
            Files.createDirectories(reportPath);

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setGraphContext(context);
            for (String inputPath : inputPaths) {
                processorConfig.addInputPath(Paths.get(inputPath));
            }
            processorConfig.setOutputDirectory(reportPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, false);
            processorConfig.setOptionValue(KeepWorkDirsOption.NAME, true);

            processor.execute(processorConfig);

            TechnologyUsageStatisticsService service = new TechnologyUsageStatisticsService(context);
            int numberFound = 0;
            boolean foundMigrationSupportModule = false;
            for (TechnologyUsageStatisticsModel model : service.findAllByProperty(TechnologyUsageStatisticsModel.NAME, SHARED_LIBS_TECH)) {
                LOG.info("Technology Usage Statistics Model: " + model);
                ProjectModel project = model.getProjectModel();
                if (project.getRootFileModel() != null && project.getRootFileModel().getFileName().equals("migration-support-1.0.0.jar"))
                    foundMigrationSupportModule = true;
                LOG.info("Project: " + project.getRootFileModel().getFileName());
                numberFound++;
            }

            Assert.assertEquals(1, numberFound);
            Assert.assertTrue(foundMigrationSupportModule);
        }
    }

    @Singleton
    public static class TechnologyIdentifiedRuleProvider extends AbstractRuleProvider {
        public TechnologyIdentifiedRuleProvider() {
            super(MetadataBuilder.forProvider(TechnologyIdentifiedRuleProvider.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule().when(
                            JavaClass.references("org.jboss.forge.furnace.{*}").inType("{*}").at(TypeReferenceLocation.IMPORT)
                    ).perform(
                            TechnologyIdentified.named("test-tech").withTag("test-tag")
                    )
                    .addRule().when(
                            FileContent.matches("{content}").inFileNamed("pom.properties")
                    ).perform(
                            TechnologyIdentified.named(SHARED_LIBS_TECH).withTag("shared-libs-tech-tag")
                    ).where("content").matches(".*migration-support.*");
        }
        // @formatter:on

    }
}
