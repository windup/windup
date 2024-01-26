package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.output.UIMessage;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.metadata.TechnologyReference;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.InputPathOption;
import org.jboss.windup.exec.configuration.options.OutputPathOption;
import org.jboss.windup.exec.configuration.options.OverwriteOption;
import org.jboss.windup.exec.configuration.options.TargetOption;
import org.jboss.windup.exec.configuration.options.UserIgnorePathOption;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.ui.WindupCommand;
import org.jboss.windup.util.PathUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;

@RunWith(Arquillian.class)
public class WindupCommandTest {
    @Deployment
    @AddonDependencies({
            @AddonDependency(name = "org.jboss.windup.ui:windup-ui"),
            @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
            @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
            @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
            @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
            @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
            @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
    })
    public static AddonArchive getDeployment() {
        return ShrinkWrap
                .create(AddonArchive.class)
                .addBeansXML()
                .addAsResource(WindupCommandTest.class.getResource("/test.jar"), "/test.jar")
                .addAsResource(WindupCommandTest.class.getResource("/ignore/test-windup-ignore.txt"), TEST_IGNORE_FILE);
    }

    private static String TEST_IGNORE_FILE = "/test.txt";

    @Inject
    private UITestHarness uiTestHarness;

    // This is just there to make sure that we have at least one "target" technology available
    @Inject
    private SampleProviderForTarget provider;

    @Before
    public void beforeTest() {
        if (System.getProperty("forge.home") == null) {
            String defaultForgeHomePath = Paths.get(OperatingSystemUtils.getTempDirectory().getAbsolutePath())
                    .resolve("Windup")
                    .resolve("fakeforgehome_" + RandomStringUtils.randomAlphanumeric(6)).toString();
            System.setProperty("forge.home", defaultForgeHomePath);
        }
    }

    @Test
    public void testOutputDirCannotBeParentOfInputDir() throws Exception {
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File tempDir = OperatingSystemUtils.createTempDir();
            File inputFile = File.createTempFile("windupwizardtest", ".jar", tempDir);
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(inputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            try {
                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                controller.setValueFor(TargetOption.NAME, Collections.singletonList("eap"));
                controller.setValueFor(InputPathOption.NAME, Collections.singletonList(inputFile));
                Assert.assertTrue(controller.canExecute());
                controller.setValueFor(OutputPathOption.NAME, tempDir);
                Assert.assertFalse(controller.canExecute());
                List<UIMessage> messages = controller.validate();
                boolean validationFound = false;
                for (UIMessage message : messages) {
                    if (message.getDescription().equals("Output path must not be a parent of input path.")) {
                        validationFound = true;
                        break;
                    }
                }
                Assert.assertTrue(validationFound);
                controller.setValueFor(OutputPathOption.NAME, null);
                Assert.assertTrue(controller.canExecute());
                controller.setValueFor(OverwriteOption.NAME, true);
            } finally {
                FileUtils.deleteQuietly(tempDir);
            }
        }
    }

    @Test
    public void testOverwriteConfirmation() throws Exception {
        String overwritePromptMessage = "Overwrite all contents of .*\\?";

        // Sets the overwrite response flag to false
        uiTestHarness.getPromptResults().put(overwritePromptMessage, "false");

        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File inputFile = File.createTempFile("windupwizardtest", "jar");
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(inputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(inputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();
                File newFileInOutputPath = new File(reportPath, "forceoverwriteprompt");
                // make sure that at least one file is in the output
                newFileInOutputPath.createNewFile();

                setupController(controller, inputFile, reportPath);

                Result result = controller.execute();

                // make sure that it failed to run (since the user's response to the overwrite question is false)
                Assert.assertTrue(result instanceof Failed);
                Assert.assertTrue(result.getMessage().contains("overwrite not specified"));
            } finally {
                inputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    @Test
    public void testNewMigration() throws Exception {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File inputFile = File.createTempFile("windupwizardtest", ".jar");
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(inputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(inputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();

                setupController(controller, inputFile, reportPath);

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);
            } finally {
                inputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    @Test
    public void testOutputDefaultValue() throws Exception {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File inputFile = File.createTempFile("windupwizardtest", ".jar");
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(inputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            try {

                setupController(controller, inputFile, null);

                Result result = controller.execute();
                Object outputDir = controller.getValueFor("output");
                Assert.assertTrue("The output should be a folder",
                        DirectoryResource.class.isAssignableFrom(outputDir.getClass()));
                Assert.assertTrue("The output should be created inside the .report folder by default",
                        ((DirectoryResource) outputDir).getName().endsWith(".report"));
                ArrayList<File> inputDirs = (ArrayList<File>) controller.getValueFor("input");
                Assert.assertEquals(1, inputDirs.size());

                File inputDirParent = inputDirs.get(0).getParentFile();
                File child = new File(inputDirParent, ((DirectoryResource) outputDir).getName());
                Assert.assertTrue("The output should be created near the ${input} folder by default", child.exists());
                Assert.assertTrue("The output should be created near the ${input} folder by default", child.isDirectory());
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);
            } finally {
                inputFile.delete();
            }
        }
    }

    @Test
    public void testUserRulesDirMigration() throws Exception {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                File userRulesDir = FileUtils.getTempDirectory().toPath().resolve("Windup")
                        .resolve("windupcommanduserrules_" + RandomStringUtils.randomAlphanumeric(6)).toFile();
                userRulesDir.mkdirs();
                controller.setValueFor(UserRulesDirectoryOption.NAME, Collections.singletonList(userRulesDir));

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);

                WindupConfiguration windupConfiguration = (WindupConfiguration) controller.getContext()
                        .getAttributeMap()
                        .get(WindupConfiguration.class);
                List<File> resultUserSpecifiedRulesDirs = windupConfiguration.getOptionValue(UserRulesDirectoryOption.NAME);
                Assert.assertEquals(1, resultUserSpecifiedRulesDirs.size());

                Iterable<Path> allRulesPaths = windupConfiguration.getAllUserRulesDirectories();

                Path expectedUserHomeRulesDir = PathUtil.getUserRulesDir();
                Path expectedWindupHomeRulesDir = PathUtil.getWindupRulesDir();

                boolean foundUserSpecifiedPath = false;
                boolean foundUserHomeDirRulesPath = false;
                boolean foundWindupHomeDirRulesPath = false;
                int totalFound = 0;
                for (Path rulesPath : allRulesPaths) {
                    totalFound++;
                    if (rulesPath.equals(userRulesDir.toPath())) {
                        foundUserSpecifiedPath = true;
                    }
                    if (rulesPath.equals(expectedUserHomeRulesDir)) {
                        foundUserHomeDirRulesPath = true;
                    }
                    if (rulesPath.equals(expectedWindupHomeRulesDir)) {
                        foundWindupHomeDirRulesPath = true;
                    }
                }
                Assert.assertTrue(foundUserSpecifiedPath);
                Assert.assertTrue(foundUserHomeDirRulesPath);
                Assert.assertTrue(foundWindupHomeDirRulesPath);
                Assert.assertEquals(3, totalFound);
            } finally {
                outputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    @Test
    public void testDuplicateUserRulesDirMigration() throws Exception {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                Path expectedUserHomeRulesDir = PathUtil.getUserRulesDir();
                expectedUserHomeRulesDir.toFile().mkdirs();
                controller.setValueFor(UserRulesDirectoryOption.NAME, Collections.singletonList(expectedUserHomeRulesDir.toFile()));

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);

                WindupConfiguration windupConfiguration = (WindupConfiguration) controller.getContext()
                        .getAttributeMap()
                        .get(WindupConfiguration.class);
                Collection<File> resultUserSpecifiedRulesDirs = windupConfiguration.getOptionValue(UserRulesDirectoryOption.NAME);

                Assert.assertEquals(expectedUserHomeRulesDir.toFile(), resultUserSpecifiedRulesDirs.iterator().next());

                Iterable<Path> allRulesPaths = windupConfiguration.getAllUserRulesDirectories();

                Path expectedWindupHomeRulesDir = PathUtil.getWindupRulesDir();

                boolean foundUserHomeDirRulesPath = false;
                boolean foundWindupHomeDirRulesPath = false;
                int totalFound = 0;
                for (Path rulesPath : allRulesPaths) {
                    totalFound++;
                    if (rulesPath.equals(expectedUserHomeRulesDir)) {
                        foundUserHomeDirRulesPath = true;
                    }
                    if (rulesPath.equals(expectedWindupHomeRulesDir)) {
                        foundWindupHomeDirRulesPath = true;
                    }
                }
                Assert.assertTrue(foundUserHomeDirRulesPath);
                Assert.assertTrue(foundWindupHomeDirRulesPath);
                Assert.assertEquals(2, totalFound);
            } finally {
                outputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    @Test
    public void testUserIgnoreDirMigration() throws Exception {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class)) {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            File inputIgnoreFile = File.createTempFile("generated-windup-ignore", ".txt");
            inputIgnoreFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar")) {
                try (OutputStream oStream = new FileOutputStream(outputFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }
            try (InputStream iStream = getClass().getResourceAsStream(TEST_IGNORE_FILE)) {
                try (OutputStream oStream = new FileOutputStream(inputIgnoreFile)) {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                controller.setValueFor(UserIgnorePathOption.NAME, inputIgnoreFile);

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);

                WindupConfiguration windupConfiguration = (WindupConfiguration) controller.getContext()
                        .getAttributeMap()
                        .get(WindupConfiguration.class);
                File resultIgnoreFile = windupConfiguration.getOptionValue(UserIgnorePathOption.NAME);
                Assert.assertEquals(inputIgnoreFile, resultIgnoreFile);

                Iterable<Path> allIgnoreDirectories = windupConfiguration.getAllIgnoreDirectories();

                Path expectedUserHomeIgnoreDir = PathUtil.getUserIgnoreDir();
                Path expectedWindupHomeIgnoreDir = PathUtil.getWindupIgnoreDir();

                boolean foundUserSpecifiedPath = false;
                boolean foundUserHomeDirIgnorePath = false;
                boolean foundWindupHomeDirIgnorePath = false;
                int totalFound = 0;
                for (Path rulesPath : allIgnoreDirectories) {
                    totalFound++;
                    if (rulesPath.equals(resultIgnoreFile.toPath())) {
                        foundUserSpecifiedPath = true;
                    }
                    if (rulesPath.equals(expectedUserHomeIgnoreDir)) {
                        foundUserHomeDirIgnorePath = true;
                    }
                    if (rulesPath.equals(expectedWindupHomeIgnoreDir)) {
                        foundWindupHomeDirIgnorePath = true;
                    }
                }
                Assert.assertTrue(foundUserSpecifiedPath);
                Assert.assertTrue(foundUserHomeDirIgnorePath);
                Assert.assertTrue(foundWindupHomeDirIgnorePath);
                Assert.assertEquals(3, totalFound);
                GraphContext context = (GraphContext) controller.getContext().getAttributeMap().get(GraphContext.class);
                GraphService<FileModel> service = new GraphService<>(context.load(), FileModel.class);
                Iterable<FileModel> findAll = service.findAll();
                boolean notEmpty = false;
                for (FileModel fileModel : findAll) {
                    notEmpty = true;
                    if (!(fileModel instanceof IgnoredFileModel) && (fileModel.getFileName().contains("META-INF"))) {
                        Assert.fail("The file " + fileModel.getFileName() + " should be ignored");
                    }
                }
                Assert.assertTrue("There should be some file models present in the graph", notEmpty);
            } finally {
                outputFile.delete();
                FileUtils.deleteQuietly(reportPath);
            }
        }
    }

    private void setupController(CommandController controller, File inputFile, File outputFile) throws Exception {
        controller.initialize();
        Assert.assertTrue(controller.isEnabled());
        controller.setValueFor(InputPathOption.NAME, Collections.singletonList(inputFile)); // FORGE-2524
        final Object value = controller.getValueFor(InputPathOption.NAME);
        Assume.assumeTrue(value instanceof Collection);
        Assume.assumeTrue(((Collection) value).iterator().hasNext());
        Assume.assumeTrue(((Collection) value).iterator().next() instanceof File);
        Assume.assumeTrue(((Collection) value).iterator().next().equals(inputFile));

        if (outputFile != null) {
            controller.setValueFor(OutputPathOption.NAME, outputFile);
        }
        controller.setValueFor(TargetOption.NAME, Collections.singletonList("eap"));

        Assert.assertTrue(controller.canExecute());
        controller.setValueFor("packages", "org.jboss");
        Assert.assertTrue(controller.canExecute());
    }

    /**
     * This class exists purely to guarantee that we have at least one selection available for "target".
     */
    @Singleton
    public static class SampleProviderForTarget extends AbstractRuleProvider {
        public SampleProviderForTarget() {
            super(MetadataBuilder.forProvider(SampleProviderForTarget.class)
                    .addTag("tag2")
                    .addSourceTechnology(new TechnologyReference("foo", "[1.0,)"))
                    .addTargetTechnology(new TechnologyReference("eap", "[1.0,)")));
        }

        @Override
        public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
            return ConfigurationBuilder.begin();
        }
    }
}
