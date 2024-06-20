package org.jboss.windup.graph;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.exec.WindupProcessor;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@RunWith(Arquillian.class)
public class ProjectModelTraversalTest {
    @Inject
    private GraphContextFactory factory;
    @Inject
    private WindupProcessor processor;

    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.reporting:windup-reporting"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap.create(AddonArchive.class).addBeansXML();
    }

    private Path getTempDirectory() {
        return FileUtils.getTempDirectory()
                .toPath()
                .resolve("Windup")
                .resolve("ProjectTraversalTest_" + RandomStringUtils.randomAlphanumeric(6));
    }

    private GraphContext createGraphContext(Path temporaryDirectory) {
        return factory.create(temporaryDirectory.resolve("output"), true);
    }

    @Test
    public void testProjectDuplicateTraversal() throws Exception {
        Path tempDirectory = getTempDirectory();

        try (GraphContext context = createGraphContext(tempDirectory)) {
            final Path inputPath1 = Paths.get("../../test-files/jee-example-app-1.0.0.ear");
            final Path inputPath2 = tempDirectory.resolve("copy-of-ear.ear");
            FileUtils.copyFile(inputPath1.toFile(), inputPath2.toFile());

            runTest(context, Arrays.asList(inputPath1.toString(), inputPath2.toString()));

            final List<String> log4jPathList = new ArrayList<>();
            final List<String> migrationSupportPathList = new ArrayList<>();
            FileFoundCallback fileFoundCallback = new FileFoundCallback() {
                @Override
                public void fileFound(ProjectModelTraversal traversal, FileModel fileModel) {
                    if (fileModel.getFileName().contains("log4j-1.2.6.jar"))
                        log4jPathList.add(traversal.getFilePath(fileModel));
                    else if (fileModel.getFileName().contains("migration-support-1.0.0.jar"))
                        migrationSupportPathList.add(traversal.getFilePath(fileModel));
                }
            };

            WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(context);
            for (FileModel inputApplication : windupConfiguration.getInputPaths()) {
                System.out.println("---------------------------------------------");
                System.out.println("Input App: " + inputApplication.getFileName() + ", project: " + inputApplication.getProjectModel().getName());
                traverse(new ProjectModelTraversal(inputApplication.getProjectModel()), 0, fileFoundCallback);
            }

            Assert.assertTrue(log4jPathList.contains("jee-example-app-1.0.0.ear/jee-example-web.war/WEB-INF/lib/log4j-1.2.6.jar"));
            Assert.assertTrue(log4jPathList.contains("copy-of-ear.ear/jee-example-web.war/WEB-INF/lib/log4j-1.2.6.jar"));
            Assert.assertTrue(log4jPathList.contains("shared-libs/jee-example-web.war/WEB-INF/lib/log4j-1.2.6.jar"));
            Assert.assertTrue(log4jPathList.contains("jee-example-app-1.0.0.ear/log4j-1.2.6.jar"));
            Assert.assertTrue(log4jPathList.contains("copy-of-ear.ear/log4j-1.2.6.jar"));
            Assert.assertTrue(log4jPathList.contains("shared-libs/log4j-1.2.6.jar"));

            Assert.assertTrue(migrationSupportPathList.contains("jee-example-app-1.0.0.ear/jee-example-web.war/WEB-INF/lib/migration-support-1.0.0.jar"));
            Assert.assertTrue(migrationSupportPathList.contains("copy-of-ear.ear/jee-example-web.war/WEB-INF/lib/migration-support-1.0.0.jar"));
            Assert.assertTrue(migrationSupportPathList.contains("shared-libs/jee-example-web.war/WEB-INF/lib/migration-support-1.0.0.jar"));
            Assert.assertTrue(migrationSupportPathList.contains("jee-example-app-1.0.0.ear/migration-support-1.0.0.jar"));
            Assert.assertTrue(migrationSupportPathList.contains("copy-of-ear.ear/migration-support-1.0.0.jar"));
            Assert.assertTrue(migrationSupportPathList.contains("shared-libs/migration-support-1.0.0.jar"));
            System.out.println("Done!");
        } finally {
            //FileUtils.deleteDirectory(tempDirectory.toFile());
        }
    }

    @Test
    public void testDuplicateFilesWithDifferingNames() throws Exception {
        Path tempDirectory = getTempDirectory();
        try (GraphContext context = createGraphContext(tempDirectory)) {
            final Path inputPath1 = Paths.get("src/test/resources/project_model_traversal/app.ear");

            runTest(context, Collections.singleton(inputPath1.toString()));

            final List<String> pathList = new ArrayList<>();
            FileFoundCallback fileFoundCallback = new FileFoundCallback() {
                @Override
                public void fileFound(ProjectModelTraversal traversal, FileModel fileModel) {
                    if (!(fileModel instanceof ArchiveModel))
                        return;

                    String path = traversal.getFilePath(fileModel);

                    pathList.add(path);
                }
            };

            WindupConfigurationModel windupConfiguration = WindupConfigurationService.getConfigurationModel(context);
            for (FileModel inputApplication : windupConfiguration.getInputPaths()) {
                System.out.println("---------------------------------------------");
                System.out.println("Input App: " + inputApplication.getFileName() + ", project: " + inputApplication.getProjectModel().getName());
                traverseRoots(new ProjectModelTraversal(inputApplication.getProjectModel()), fileFoundCallback);
            }

            Assert.assertTrue(pathList.contains("app.ear/xercesImpl-2.11.0.jar"));
            Assert.assertTrue(pathList.contains("app.ear/xercesImpl-other.jar"));
            System.out.println("Done!");
        } finally {
            FileUtils.deleteQuietly(tempDirectory.toFile());
        }
    }

    private void traverseRoots(ProjectModelTraversal traversal, FileFoundCallback callback) {
        callback.fileFound(traversal, traversal.getCurrent().getRootFileModel());

        for (ProjectModelTraversal child : traversal.getChildren()) {
            traverseRoots(child, callback);
        }
    }

    private void traverse(ProjectModelTraversal traversal, int indentLevel, FileFoundCallback callback) {
        String indent = StringUtils.repeat(" ", indentLevel * 3);

        System.out.println(indent + "Project: " + traversal.getCanonicalProject().getName());
        System.out.println("Root file: " + traversal.getFilePath(traversal.getCurrent().getRootFileModel()));
        System.out.println(indent + "Files: ");
        for (FileModel fileModel : traversal.getCanonicalProject().getFileModels()) {
            System.out.println(indent + "   " + traversal.getFilePath(fileModel));
            callback.fileFound(traversal, fileModel);
        }

        for (ProjectModelTraversal child : traversal.getChildren()) {
            traverse(child, indentLevel + 1, callback);
        }
    }

    void runTest(final GraphContext graphContext, Iterable<String> inputPaths) throws Exception {
        WindupConfiguration windupConfiguration = new WindupConfiguration().setGraphContext(graphContext);
        windupConfiguration.setAlwaysHaltOnException(true);
        windupConfiguration.setOptionValue(KeepWorkDirsOption.NAME, true);
        for (String inputPath : inputPaths) {
            windupConfiguration.addInputPath(Paths.get(inputPath));
        }
        windupConfiguration.setOutputDirectory(graphContext.getGraphDirectory());

        processor.execute(windupConfiguration);
    }

    private interface FileFoundCallback {
        void fileFound(ProjectModelTraversal traversal, FileModel fileModel);
    }
}
