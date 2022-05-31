package org.jboss.windup.rules.apps.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.RuleProvider;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.AndPredicate;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.decompiler.DecompileClassesRuleProvider;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ClassMetadataTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

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
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Test
    public void testJavaSourceScanning() throws Exception {
        runTest("src/test/resources/classmetadatatest/src");
    }

    @Test
    public void testJavaClassFiles() throws Exception {
        runTest("src/test/resources/classmetadatatest/sampleclasses");
    }

    private void runTest(String inputPath) throws Exception {
        try (GraphContext context = factory.create(getDefaultPath(), true)) {
            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            Predicate<RuleProvider> ruleFilter = new AndPredicate(new RuleProviderWithDependenciesPredicate(MigrationRulesPhase.class),
                    new NotPredicate(new EnumeratedRuleProviderPredicate(DecompileClassesRuleProvider.class)));

            processorConfig.setRuleProviderFilter(ruleFilter);
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputPath));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));

            processor.execute(processorConfig);

            JavaClassService javaClassService = new JavaClassService(context);
            JavaClassModel javaClassModel = javaClassService.getByName("com.rhc.booking.services.EventServer");
            Assert.assertNotNull(javaClassModel);
            Assert.assertNull(javaClassModel.getExtends());
            Assert.assertNotNull(javaClassModel.getInterfaces());
            Assert.assertTrue(javaClassModel.getInterfaces().iterator().hasNext());
            Assert.assertTrue(javaClassModel.isInterface());

            int interfaceCountFound = 0;
            for (JavaClassModel interfce : javaClassModel.getInterfaces()) {
                interfaceCountFound++;
                Assert.assertEquals("java.rmi.Remote", interfce.getQualifiedName());
            }
            Assert.assertEquals(1, interfaceCountFound);
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_classmetadatatest_" + RandomStringUtils.randomAlphanumeric(6));
    }

}
