package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.exec.configuration.options.UserRulesDirectoryOption;
import org.jboss.windup.graph.GraphContextFactory;
import org.jboss.windup.ui.WindupCommand;
import org.jboss.windup.util.WindupPathUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupCommandTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup:ui"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.utils:utils"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupCommandTest.class.getResource("/test.jar"), "/test.jar")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup:ui"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:utils"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
                    );

        return archive;
    }

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private GraphContextFactory graphContextFactory;

    private Path forgeHome;

    @Before
    public void beforeTest()
    {
        if (System.getProperty("forge.home") == null)
        {
            String defaultForgeHomePath = Paths.get(OperatingSystemUtils.getTempDirectory().getAbsolutePath()).resolve("Windup")
                        .resolve("fakeforgehome_" + RandomStringUtils.randomAlphanumeric(6)).toString();
            System.setProperty("forge.home", defaultForgeHomePath);
        }
        this.forgeHome = Paths.get(System.getProperty("forge.home"));
    }

    @Test
    public void testOverwriteConfirmation() throws Exception
    {
        Assert.assertNotNull(uiTestHarness);

        String overwritePromptMessage = "Overwrite all contents of .*\\?";

        // Sets the overwrite response flag to false
        uiTestHarness.getPromptResults().put(overwritePromptMessage, "false");

        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class))
        {
            File inputFile = File.createTempFile("windupwizardtest", "jar");
            inputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar"))
            {
                try (OutputStream oStream = new FileOutputStream(inputFile))
                {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(inputFile.getAbsoluteFile() + "_output");
            try
            {
                reportPath.mkdirs();
                File newFileInOutputPath = new File(reportPath, "forceoverwriteprompt");
                // make sure that at least one file is in the output
                newFileInOutputPath.createNewFile();

                setupController(controller, inputFile, reportPath);

                Result result = controller.execute();

                // make sure that it failed to run (since the user's response to the overwrite question is false)
                Assert.assertTrue(result instanceof Failed);
                Assert.assertTrue(result.getMessage().contains("Windup execution aborted"));
            }
            finally
            {
                inputFile.delete();
                FileUtils.deleteDirectory(reportPath);
            }
        }
    }

    @Test
    public void testNewMigration() throws Exception
    {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class))
        {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar"))
            {
                try (OutputStream oStream = new FileOutputStream(outputFile))
                {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try
            {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);
            }
            finally
            {
                outputFile.delete();
                FileUtils.deleteDirectory(reportPath);
            }
        }
    }

    @Test
    public void testUserRulesDirMigration() throws Exception
    {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class))
        {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar"))
            {
                try (OutputStream oStream = new FileOutputStream(outputFile))
                {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try
            {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                File userRulesDir = FileUtils.getTempDirectory().toPath().resolve("Windup")
                            .resolve("windupcommanduserrules_" + RandomStringUtils.randomAlphanumeric(6)).toFile();
                userRulesDir.mkdirs();
                controller.setValueFor("userRulesDirectory", userRulesDir);

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);

                WindupConfiguration windupConfiguration = (WindupConfiguration) controller.getContext().getAttributeMap()
                            .get(WindupConfiguration.class);
                File resultUserSpecifiedRulesDir = windupConfiguration.getOptionValue(UserRulesDirectoryOption.NAME);
                Assert.assertEquals(userRulesDir, resultUserSpecifiedRulesDir);

                Iterable<Path> allRulesPaths = windupConfiguration.getAllUserRulesDirectories();

                Path expectedUserHomeRulesDir = WindupPathUtil.getWindupUserRulesDir();
                Path expectedWindupHomeRulesDir = WindupPathUtil.getWindupHomeRules();

                boolean foundUserSpecifiedPath = false;
                boolean foundUserHomeDirRulesPath = false;
                boolean foundWindupHomeDirRulesPath = false;
                int totalFound = 0;
                for (Path rulesPath : allRulesPaths)
                {
                    totalFound++;
                    if (rulesPath.equals(userRulesDir.toPath()))
                    {
                        foundUserSpecifiedPath = true;
                    }
                    if (rulesPath.equals(expectedUserHomeRulesDir))
                    {
                        foundUserHomeDirRulesPath = true;
                    }
                    if (rulesPath.equals(expectedWindupHomeRulesDir))
                    {
                        foundWindupHomeDirRulesPath = true;
                    }
                }
                Assert.assertTrue(foundUserSpecifiedPath);
                Assert.assertTrue(foundUserHomeDirRulesPath);
                Assert.assertTrue(foundWindupHomeDirRulesPath);
                Assert.assertEquals(3, totalFound);
            }
            finally
            {
                outputFile.delete();
                FileUtils.deleteDirectory(reportPath);
            }
        }
    }

    @Test
    public void testDuplicateUserRulesDirMigration() throws Exception
    {
        Assert.assertNotNull(uiTestHarness);
        try (CommandController controller = uiTestHarness.createCommandController(WindupCommand.class))
        {
            File outputFile = File.createTempFile("windupwizardtest", ".jar");
            outputFile.deleteOnExit();
            try (InputStream iStream = getClass().getResourceAsStream("/test.jar"))
            {
                try (OutputStream oStream = new FileOutputStream(outputFile))
                {
                    IOUtils.copy(iStream, oStream);
                }
            }

            File reportPath = new File(outputFile.getAbsoluteFile() + "_output");
            try
            {
                reportPath.mkdirs();

                setupController(controller, outputFile, reportPath);

                Path expectedUserHomeRulesDir = WindupPathUtil.getWindupUserRulesDir();
                expectedUserHomeRulesDir.toFile().mkdirs();
                controller.setValueFor("userRulesDirectory", expectedUserHomeRulesDir.toFile());

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);

                WindupConfiguration windupConfiguration = (WindupConfiguration) controller.getContext().getAttributeMap()
                            .get(WindupConfiguration.class);
                File resultUserSpecifiedRulesDir = windupConfiguration.getOptionValue(UserRulesDirectoryOption.NAME);
                Assert.assertEquals(expectedUserHomeRulesDir.toFile(), resultUserSpecifiedRulesDir);

                Iterable<Path> allRulesPaths = windupConfiguration.getAllUserRulesDirectories();

                Path expectedWindupHomeRulesDir = WindupPathUtil.getWindupHomeRules();

                boolean foundUserHomeDirRulesPath = false;
                boolean foundWindupHomeDirRulesPath = false;
                int totalFound = 0;
                for (Path rulesPath : allRulesPaths)
                {
                    totalFound++;
                    if (rulesPath.equals(expectedUserHomeRulesDir))
                    {
                        foundUserHomeDirRulesPath = true;
                    }
                    if (rulesPath.equals(expectedWindupHomeRulesDir))
                    {
                        foundWindupHomeDirRulesPath = true;
                    }
                }
                Assert.assertTrue(foundUserHomeDirRulesPath);
                Assert.assertTrue(foundWindupHomeDirRulesPath);
                Assert.assertEquals(2, totalFound);
            }
            finally
            {
                outputFile.delete();
                FileUtils.deleteDirectory(reportPath);
            }
        }
    }

    private void setupController(CommandController controller, File inputFile, File outputFile) throws Exception
    {
        controller.initialize();
        Assert.assertTrue(controller.isEnabled());
        controller.setValueFor("input", inputFile);
        Assert.assertFalse(controller.canExecute());
        controller.setValueFor("output", outputFile);
        Assert.assertTrue(controller.canExecute());
        controller.setValueFor("packages", "org.jboss");
        Assert.assertTrue(controller.canExecute());
    }

}
