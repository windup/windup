package org.jboss.windup.test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import org.jboss.windup.WindupLegacyService;
import org.jboss.windup.impl.ui.WindupLegacyWizard;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupLegacyWizardTest
{
    private static final String SAMPLE_FILE_DATA = "<?xml version=\"1.0\"?>\n<test></test>";

    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.legacy.application:legacy-windup"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupLegacyWizardTest.class.getResource("/test.jar"), "/test.jar")
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness"),
                                AddonDependencyEntry.create("org.jboss.windup.legacy.application:legacy-windup")
                    );

        return archive;
    }

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private WindupLegacyService windupService;

    @Test
    public void testNewMigration() throws Exception
    {
        Assert.assertNotNull(windupService);
        Assert.assertNotNull(uiTestHarness);
        try (WizardCommandController controller = uiTestHarness.createWizardController(WindupLegacyWizard.class))
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

                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                controller.setValueFor("input", inputFile);
                Assert.assertFalse(controller.canExecute());
                controller.setValueFor("output", reportPath);
                Assert.assertFalse(controller.canExecute());
                controller.setValueFor("packages", "org.jboss");
                Assert.assertTrue(controller.canExecute());
                
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
}
