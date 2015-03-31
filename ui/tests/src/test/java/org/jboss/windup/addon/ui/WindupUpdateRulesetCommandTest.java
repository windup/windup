package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import net.lingala.zip4j.core.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.dependencies.DependencyResolver;
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
import org.jboss.windup.ui.RulesetUpdateChecker;
import org.jboss.windup.ui.WindupUpdateRulesetCommand;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupUpdateRulesetCommandTest
{
    @Deployment
    @Dependencies({
                @AddonDependency(name = "org.jboss.windup.ui:windup-ui"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.addon:maven"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static ForgeArchive getDeployment()
    {
        ForgeArchive archive = ShrinkWrap
                    .create(ForgeArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupCommandTest.class.getResource(TEST_OLD_WINDUP), TEST_OLD_WINDUP)
                    .addAsAddonDependencies(
                                AddonDependencyEntry.create("org.jboss.windup.ui:windup-ui"),
                                AddonDependencyEntry.create("org.jboss.windup.exec:windup-exec"),
                                AddonDependencyEntry.create("org.jboss.windup.graph:windup-graph"),
                                AddonDependencyEntry.create("org.jboss.windup.rules.apps:windup-rules-java"),
                                AddonDependencyEntry.create("org.jboss.windup.utils:windup-utils"),
                                AddonDependencyEntry.create("org.jboss.forge.furnace.container:cdi"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:maven"),
                                AddonDependencyEntry.create("org.jboss.forge.addon:ui-test-harness")
                    );

        return archive;
    }

    private static String TEST_OLD_WINDUP = "/windup-old-ruleset.zip";

    @Inject
    private DependencyResolver resolver;
    
    @Inject
    private UITestHarness uiTestHarness;

    @Test
    public void testOutputDirCannotBeParentOfInputDir() throws Exception
    {
        File tempDir = OperatingSystemUtils.createTempDir();
        File inputFile = File.createTempFile("windup-old-ruleset", ".zip", tempDir);
        inputFile.deleteOnExit();
        try (InputStream iStream = getClass().getResourceAsStream(TEST_OLD_WINDUP))
        {
            try (OutputStream oStream = new FileOutputStream(inputFile))
            {
                IOUtils.copy(iStream, oStream);
            }
        }
        try (CommandController controller = uiTestHarness.createCommandController(WindupUpdateRulesetCommand.class))
        {
            ZipFile zipFile = new ZipFile(inputFile.getAbsolutePath());
            String extractedFolderPath = tempDir.getAbsolutePath() + "/extracted-windup";
            new File(extractedFolderPath).mkdirs();
            zipFile.extractAll(extractedFolderPath);
            String windupHome = extractedFolderPath + "/windup-old-ruleset";
            System.setProperty("windup.home", windupHome);
            boolean rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver);
            Assert.assertTrue(rulesetNeedUpdate);
            try
            {
                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                Result result = controller.execute();
                Assert.assertFalse(result instanceof Failed);
                rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver);
                Assert.assertFalse(rulesetNeedUpdate);
            }
            finally
            {
                FileUtils.deleteDirectory(tempDir);
            }
        }
    }

}