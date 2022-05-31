package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ast.java.data.ResolutionStatus;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.EnableClassNotFoundAnalysisOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class JavaClassBindingStatusTest {
    private static final String METADATA_FILENAME = "JavaClassBindingStatusTest.technology.metadata.xml";
    private static final String JAR_FILENAME = "commons-io-2.1.jar";
    @Inject
    private GraphContextFactory factory;
    @Inject
    private WindupProcessor processor;
    @Inject
    private ClassBindingTestRuleProvider provider;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(new File("src/test/resources/" + METADATA_FILENAME), METADATA_FILENAME)
                .addAsResource(new File("src/test/resources/lib/" + JAR_FILENAME), JAR_FILENAME);

    }

    @Test
    public void testResolutionStatus() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            final WindupConfiguration processorConfig = new WindupConfiguration().setOutputDirectory(outputPath);

            Path rulesPath = FileUtils.getTempDirectory().toPath().resolve("Windup")
                    .resolve("testrulespath_" + RandomStringUtils.randomAlphanumeric(6));
            System.out.println("Using rules path: " + rulesPath);
            Files.createDirectories(rulesPath);
            try (InputStream is = getClass().getResourceAsStream("/" + METADATA_FILENAME);
                 OutputStream os = new FileOutputStream(rulesPath.resolve(METADATA_FILENAME).toFile())) {
                IOUtils.copy(is, os);
            }
            Path rulesLibPath = rulesPath.resolve("lib");
            Files.createDirectories(rulesLibPath);
            try (InputStream is = getClass().getResourceAsStream("/" + JAR_FILENAME);
                 OutputStream os = new FileOutputStream(rulesLibPath.resolve(JAR_FILENAME).toFile())) {
                IOUtils.copy(is, os);
            }

            WindupConfigurationModel windupConfigurationModel = WindupConfigurationService.getConfigurationModel(context);
            windupConfigurationModel.addUserRulesPath(new FileService(context).createByFilePath(rulesPath.toString()));

            processorConfig.setOptionValue(TargetOption.NAME, Collections.singletonList("fakeserver"));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(EnableClassNotFoundAnalysisOption.NAME, true);
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context, JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();
            Assert.assertTrue(typeReferences.iterator().hasNext());

            boolean nioPathResolved = false;
            boolean fileUtilsResolved = false;
            boolean fileModelRecovered = false;
            for (JavaTypeReferenceModel typeReference : typeReferences) {
                if (typeReference.getResolvedSourceSnippit().equals("Path"))
                    Assert.fail("Path should resolve to java.nio.Path");

                if (typeReference.getResolvedSourceSnippit().equals("java.nio.file.Path")) {
                    if (typeReference.getResolutionStatus() != ResolutionStatus.RESOLVED)
                        Assert.fail("Failed to resolve java.nio.Path");
                    else
                        nioPathResolved = true;
                } else if (typeReference.getResolvedSourceSnippit().equals("org.apache.commons.io.FileUtils")) {
                    if (typeReference.getResolutionStatus() != ResolutionStatus.RESOLVED)
                        Assert.fail("Failed to resolve Commons IO FileUtils");
                    else
                        fileUtilsResolved = true;
                } else if (typeReference.getResolvedSourceSnippit().equals("org.jboss.windup.graph.model.resource.FileModel")) {
                    if (typeReference.getResolutionStatus() == ResolutionStatus.RESOLVED)
                        Assert.fail("FileModel is not on the library path and should not be resolved");
                    else
                        fileModelRecovered = true;
                }
            }
            Assert.assertTrue(nioPathResolved);
            Assert.assertTrue(fileUtilsResolved);
            Assert.assertTrue(fileModelRecovered);
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("JavaClassBindingStatusTest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    @Singleton
    public static class ClassBindingTestRuleProvider extends AbstractRuleProvider {
        public ClassBindingTestRuleProvider() {
            super(MetadataBuilder.forProvider(ClassBindingTestRuleProvider.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
        }

        // @formatter:off
        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin()
                    .addRule()
                    .when(
                            JavaClass.references("java.nio.file.{*}")
                                    .or(JavaClass.references("org.apache.commons.io.{*}"))
                    )
                    .perform(Hint.withText("Test Text").withEffort(1));
        }
        // @formatter:on
    }
}