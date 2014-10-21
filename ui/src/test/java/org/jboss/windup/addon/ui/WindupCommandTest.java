package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
import org.jboss.windup.ui.WindupCommand;
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
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:rules-java", version = "2.0.0-SNAPSHOT"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
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
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
                    );

        return archive;
    }

    @Inject
    private UITestHarness uiTestHarness;

    @Before
    public void beforeTest()
    {
        if (System.getProperty("forge.home") == null)
        {
            String defaultForgeHomePath = Paths.get(OperatingSystemUtils.getTempDirectory().getAbsolutePath()).resolve("Windup")
                        .resolve("fakeforgehome_" + RandomStringUtils.randomAlphanumeric(6)).toString();
            System.setProperty("forge.home", defaultForgeHomePath);
        }
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
