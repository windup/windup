package org.jboss.windup.rules.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.predicates.EnumeratedRuleProviderPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.rulefilters.NotPredicate;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.config.EnableClassNotFoundAnalysisOption;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.provider.FindUnboundJavaReferencesRuleProvider;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests the javaclass condition along with hint-not-exists condition.
 */
@RunWith(Arquillian.class)
public class JavaClassWithoutHintTest {
    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource("org/jboss/windup/rules/java/javaclass-withouthint.windup.xml");
    }

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testJavaClassCondition() throws IOException, InstantiationException, IllegalAccessException {
        final Path outputPath = getDefaultPath();
        FileUtils.deleteDirectory(outputPath.toFile());
        Files.createDirectories(outputPath);

        try (GraphContext context = factory.create(outputPath, true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final WindupConfiguration processorConfig = new WindupConfiguration().setOutputDirectory(outputPath);
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(EnableClassNotFoundAnalysisOption.NAME, true);
            processorConfig.setRuleProviderFilter(new NotPredicate(new EnumeratedRuleProviderPredicate(FindUnboundJavaReferencesRuleProvider.class)));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context, JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();

            int count = 0;
            for (JavaTypeReferenceModel ref : typeReferences) {
                String sourceSnippit = ref.getResolvedSourceSnippit();
                if (sourceSnippit.contains("org.jboss"))
                    count++;
            }
            Assert.assertTrue(count > 9);

            GraphService<InlineHintModel> hintService = new GraphService<>(context, InlineHintModel.class);
            Iterable<InlineHintModel> hints = hintService.findAll();

            count = 0;
            for (InlineHintModel hint : hints) {
                if (hint.getHint().contains("Rule1"))
                    count++;
            }
            Assert.assertEquals(1, count);

            count = 0;
            for (InlineHintModel hint : hints) {
                if (hint.getHint().contains("Rule2")) {
                    if (hint.getFileLocationReference().getSourceSnippit().contains("org.jboss.forge.furnace.util.Callables")) {
                        Assert.assertTrue(hint.getHint().contains("Callables"));
                        count++;
                    } else
                        Assert.fail("Catch-all hint should not be registered for locations that already contain hint");
                }

            }
            Assert.assertEquals(1, count);

            count = 0;
            for (InlineHintModel hint : hints) {
                if (hint.getHint().contains("Rule3")) {
                    if (hint.getFileLocationReference().getSourceSnippit().contains("org.jboss.windup.exec.configuration.WindupConfiguration"))
                        count++;
                    else
                        Assert.fail("Catch-all hint should not be registered for locations that already contain hint");
                }

            }
            Assert.assertEquals(1, count);

            for (InlineHintModel hint : hints) {
                if (hint.getHint().contains("Rule4")) {
                    Assert.fail("Catch-all hint should not be registered for locations that already contain hint");
                }
            }

        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }
}