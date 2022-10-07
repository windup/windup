package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.engine.predicates.RuleProviderWithDependenciesPredicate;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.provider.CacheFileModelPrettyPathRuleProvider;
import org.jboss.windup.testutil.basics.WindupTestUtilMethods;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class CacheFileModelPrettyPathRuleProviderTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.config:windup-config"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.windup.tests:test-util"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi")
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    @Inject
    CacheFileModelPrettyPathRuleProvider provider;

    @Inject
    private WindupProcessor processor;

    @Inject
    private GraphContextFactory factory;

    @Test
    public void testCachedPrettyPath() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext context = factory.create(WindupTestUtilMethods.getTempDirectoryForGraph(), true)) {
            final String inputDir = "src/test/resources/org/jboss/windup/rules/java";

            final Path outputPath = Paths.get(FileUtils.getTempDirectory().toString(),
                    "windup_" + RandomStringUtils.randomAlphanumeric(6));
            FileUtils.deleteDirectory(outputPath.toFile());
            Files.createDirectories(outputPath);

            ProjectModel pm = context.getFramed().addFramedVertex(ProjectModel.class);
            pm.setName("Main Project");

            FileModel inputPathFrame = context.getFramed().addFramedVertex(FileModel.class);
            inputPathFrame.setFilePath(inputDir);
            pm.addFileModel(inputPathFrame);

            pm.setRootFileModel(inputPathFrame);

            FileModel fileModel = context.getFramed().addFramedVertex(FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile1.java");
            pm.addFileModel(fileModel);

            fileModel = context.getFramed().addFramedVertex(FileModel.class);
            fileModel.setFilePath(inputDir + "/JavaClassTestFile2.java");
            pm.addFileModel(fileModel);

            context.commit();

            final WindupConfiguration processorConfig = new WindupConfiguration();
            processorConfig.setRuleProviderFilter(new RuleProviderWithDependenciesPredicate(
                    CacheFileModelPrettyPathRuleProvider.class));
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<FileModel> service = new GraphService<>(context, FileModel.class);
            boolean file1Found = false;
            boolean file2Found = false;
            boolean file3Found = false;
            boolean file4Found = false;
            boolean file5Found = false;
            for (FileModel file : service.findAll()) {
                if (file.isDirectory())
                    continue;

                System.out.println("pretty path: " + file.getCachedPrettyPath());
                if (file.getFileName().equals("JavaClassTestFile1.java")) {
                    file1Found = true;
                    Assert.assertEquals("org.jboss.windup.rules.java.JavaClassTestFile1", file.getCachedPrettyPath());
                } else if (file.getFileName().equals("JavaClassTestFile2.java")) {
                    file2Found = true;
                    Assert.assertEquals("org.jboss.windup.rules.java.JavaClassTestFile2", file.getCachedPrettyPath());
                } else if (file.getFileName().equals("javaclass-withoutclassification.windup.xml")) {
                    file3Found = true;
                } else if (file.getFileName().equals("javaclass-withouthint.windup.xml")) {
                    file4Found = true;
                    Assert.assertEquals("javaclass-withouthint.windup.xml", file.getCachedPrettyPath());
                } else if (file.getFileName().equals("JavaClassXmlRulesTest.windup.xml")) {
                    file5Found = true;
                    Assert.assertEquals("JavaClassXmlRulesTest.windup.xml", file.getCachedPrettyPath());
                }
            }
            Assert.assertTrue(file1Found);
            Assert.assertTrue(file2Found);
            Assert.assertTrue(file3Found);
            Assert.assertTrue(file4Found);
            Assert.assertTrue(file5Found);
        }
    }

    private static Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }
}
