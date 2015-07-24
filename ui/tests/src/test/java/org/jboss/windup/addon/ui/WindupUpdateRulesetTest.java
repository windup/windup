package org.jboss.windup.addon.ui;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.util.PathUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(Arquillian.class)
public class WindupUpdateRulesetTest
{
    @Deployment
    @AddonDependencies({
        @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
        @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
        @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
        @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
        @AddonDependency(name = "org.jboss.windup.ui:windup-ui"),
        @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
        @AddonDependency(name = "org.jboss.forge.addon:maven"),
        //@AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap
            .create(AddonArchive.class)
            .addBeansXML()
            .addAsResource(WindupUpdateRulesetTest.class.getResource(TEST_OLD_WINDUP), TEST_OLD_WINDUP);
        return archive;
    }

    private static String TEST_OLD_WINDUP = "/windup-old-ruleset.zip";

    @Inject
    private DependencyResolver resolver;

    @Inject
    private RulesetsUpdater updater;


    @Test
    public void testUpdateRuleset() throws Exception
    {
        // Extract the rulesets to a temp dir and move the rules/ to target/rules/ .
        File tempDir = OperatingSystemUtils.createTempDir();

        PathUtil.unzipFromResource(WindupUpdateRulesetTest.class, TEST_OLD_WINDUP, tempDir);
        final File targetDir = PathUtil.getWindupHome().resolve("target").toAbsolutePath().toFile();
        final File rulesetsDir = new File(targetDir, "rules");
        FileUtils.deleteDirectory(rulesetsDir);
        FileUtils.moveDirectoryToDirectory(new File(tempDir, "windup-old-ruleset/rules"), targetDir, false);
        System.setProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP, rulesetsDir.getAbsolutePath());
        FileUtils.deleteDirectory(tempDir);

        try
        {
            boolean rulesetNeedUpdate = this.updater.rulesetsNeedUpdate();
            Assert.assertTrue("Rulesets should need an update.", rulesetNeedUpdate);
            updater.replaceRulesetsDirectoryWithLatestReleaseIfAny();
            Assert.assertFalse("Rulesets should not need an update.", this.updater.rulesetsNeedUpdate());
        }
        catch (Throwable ex){
            if(ex.getClass().getSimpleName().equals("InvocationTargetException"))
            {
                final Throwable wrappedEx = ((InvocationTargetException)ex).getTargetException();
                throw new RuntimeException(wrappedEx.getClass().getSimpleName() + " " + wrappedEx.getMessage(), wrappedEx);
            }
            else
                throw ex;
        }
        finally
        {
            System.getProperties().remove("windup.home");
        }
    }

}