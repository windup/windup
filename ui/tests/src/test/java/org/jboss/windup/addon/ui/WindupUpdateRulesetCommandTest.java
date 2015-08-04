package org.jboss.windup.addon.ui;

import java.io.File;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.ui.WindupUpdateRulesetCommand;
import org.jboss.windup.util.ZipUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class WindupUpdateRulesetCommandTest
{
    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.windup.ui:windup-ui"),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.forge.addon:maven"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupCommandTest.class.getResource(TEST_OLD_WINDUP), TEST_OLD_WINDUP);
        return archive;
    }

    private static String TEST_OLD_WINDUP = "/windup-old-ruleset.zip";

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private RulesetsUpdater updater;

    @Test
    @Ignore("Command can't be used currently as there's no way to run it from the UI."
                + " I'm leaving it here in case we needed the command again (maybe from a GUI?).")
    public void testUpdateRulesetCommand() throws Exception
    {
        // Extract the windup zip to a temp dir.
        File tempDir = OperatingSystemUtils.createTempDir();
        ZipUtil.unzipFromClassResource(WindupUpdateRulesetCommandTest.class, TEST_OLD_WINDUP, tempDir);

        // This may cause FileNotFound in Furnace if it's already running.
        System.setProperty("windup.home", new File(tempDir, "windup-old-ruleset").getAbsolutePath());

        try (CommandController controller = uiTestHarness.createCommandController(WindupUpdateRulesetCommand.class))
        {
            boolean rulesetNeedUpdate = updater.rulesetsNeedUpdate();
            Assert.assertTrue("Rulesets should need an update.", rulesetNeedUpdate);

            controller.initialize();
            Assert.assertTrue(controller.isEnabled());
            Result result = controller.execute();
            Assert.assertFalse(result instanceof Failed);
            rulesetNeedUpdate = updater.rulesetsNeedUpdate();
            Assert.assertFalse(rulesetNeedUpdate);
        }
        finally
        {
            FileUtils.deleteDirectory(tempDir);
            System.getProperties().remove("windup.home");
        }
    }

}