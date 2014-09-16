package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.WizardCommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.Dependencies;
import org.jboss.forge.arquillian.archive.ForgeArchive;
import org.jboss.forge.furnace.repositories.AddonDependencyEntry;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ui.WindupWizard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupWizardTest
{

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup:ui"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupWizardTest.class.getResource("/test.jar"), "/test.jar")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup:ui"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
                    );

        return archive;
    }

    @Inject
    private UITestHarness uiTestHarness;

    @Test
    public void testOverwriteConfirmation() throws Exception
    {
        Assert.assertNotNull(uiTestHarness);

        String overwritePromptMessage = "Overwrite all contents of .*\\?";

        // Sets the overwrite response flag to false
        uiTestHarness.getPromptResults().put(overwritePromptMessage, "false");

        try (WizardCommandController controller = uiTestHarness.createWizardController(WindupWizard.class))
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
        try (WizardCommandController controller = uiTestHarness.createWizardController(WindupWizard.class))
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

                setupController(controller, inputFile, reportPath);

                Result result = controller.execute();
                final String msg = "controller.execute() 'Failed': " + result.getMessage();
                Assert.assertFalse(msg, result instanceof Failed);
            }
            finally
            {
                inputFile.delete();
                FileUtils.deleteDirectory(reportPath);
            }
        }
    }

    private void setupController(WizardCommandController controller, File inputFile, File outputFile) throws Exception
    {
        controller.initialize();
        Assert.assertTrue(controller.isEnabled());
        controller.setValueFor("input", inputFile);
        Assert.assertFalse(controller.canExecute());
        controller.setValueFor("output", outputFile);
        Assert.assertFalse(controller.canExecute());
        controller.setValueFor("packages", "org.jboss");
        Assert.assertTrue(controller.canExecute());
    }

}
