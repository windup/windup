package org.jboss.windup.rules.java;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.config.ScanPackagesOption;
import org.jboss.windup.rules.apps.java.config.SourceModeOption;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
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
 * Tests the {@link JavaClass} condition along with along with the has-classification condition
 */
@RunWith(Arquillian.class)
public class JavaClassWithoutClassificationTest {
    @Inject
    private WindupProcessor processor;
    @Inject
    private GraphContextFactory factory;

    @Deployment
    @AddonDependencies
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class)
                .addBeansXML()
                .addAsResource("org/jboss/windup/rules/java/javaclass-withoutclassification.windup.xml");
    }

    @Test
    public void testJavaClassCondition() throws IOException, InstantiationException, IllegalAccessException {
        try (GraphContext context = factory.create(getDefaultPath(), true)) {
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

            final WindupConfiguration processorConfig = new WindupConfiguration().setOutputDirectory(outputPath);
            processorConfig.setGraphContext(context);
            processorConfig.addInputPath(Paths.get(inputDir));
            processorConfig.setOutputDirectory(outputPath);
            processorConfig.setOptionValue(ScanPackagesOption.NAME, Collections.singletonList(""));
            processorConfig.setOptionValue(SourceModeOption.NAME, true);

            processor.execute(processorConfig);

            GraphService<JavaTypeReferenceModel> typeRefService = new GraphService<>(context,
                    JavaTypeReferenceModel.class);
            Iterable<JavaTypeReferenceModel> typeReferences = typeRefService.findAll();

            boolean foundJBossForgeSnippit = false;
            boolean foundJavaClassTestFileSnippit = false;
            for (JavaTypeReferenceModel ref : typeReferences) {
                String sourceSnippit = ref.getResolvedSourceSnippit();
                if (sourceSnippit.contains("org.jboss.forge"))
                    foundJBossForgeSnippit = true;
                if (sourceSnippit.contains("org.jboss.windup.rules.java.JavaClassTestFile"))
                    foundJavaClassTestFileSnippit = true;
            }
            Assert.assertTrue(foundJBossForgeSnippit);
            Assert.assertTrue(foundJavaClassTestFileSnippit);

            GraphService<ClassificationModel> classificationService = new GraphService<>(context, ClassificationModel.class);
            Iterable<ClassificationModel> classifications = classificationService.findAll();

            int count = 0;
            for (ClassificationModel cModel : classifications) {
                count++;
            }
            Assert.assertEquals(2, count);

            count = 0;
            for (ClassificationModel cModel : classifications) {
                if (cModel.getClassification().contains("Rule1"))
                    count++;
            }
            Assert.assertEquals(1, count);

            count = 0;
            for (ClassificationModel cModel : classifications) {
                if (cModel.getClassification().contains("Rule2"))
                    count++;
            }
            Assert.assertEquals(0, count);

            count = 0;
            for (ClassificationModel cModel : classifications) {
                if (cModel.getClassification().contains("Rule3") && cModel.getFileModels().iterator().next().getFileName().contains("File1"))
                    count++;
            }
            Assert.assertEquals(1, count);
        }
    }

    private Path getDefaultPath() {
        return FileUtils.getTempDirectory().toPath().resolve("Windup")
                .resolve("windupgraph_javaclasstest_" + RandomStringUtils.randomAlphanumeric(6));
    }
}