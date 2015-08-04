package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.forge.addon.dependencies.Coordinate;
import org.jboss.forge.addon.dependencies.DependencyResolver;
import org.jboss.forge.addon.dependencies.builder.CoordinateBuilder;
import org.jboss.forge.addon.dependencies.builder.DependencyQueryBuilder;
import org.jboss.forge.addon.ui.controller.CommandController;
import org.jboss.forge.addon.ui.result.Failed;
import org.jboss.forge.addon.ui.result.Result;
import org.jboss.forge.addon.ui.test.UITestHarness;
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.manager.AddonManager;
import org.jboss.forge.furnace.manager.request.InstallRequest;
import org.jboss.forge.furnace.manager.request.RemoveRequest;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.forge.furnace.versions.SingleVersion;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.exec.updater.RulesetsUpdater;
import org.jboss.windup.ui.DistributionUpdater;
import org.jboss.windup.ui.WindupCommand;
import org.jboss.windup.ui.WindupUpdateDistributionCommand;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.ZipUtil;
import org.jboss.windup.util.exception.WindupException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@RunWith(Arquillian.class)
public class WindupUpdateDistributionCommandTest
{
    private static final Logger log = Logging.get(WindupUpdateDistributionCommandTest.class);

    private static final String WINDUP_UI_ADDON_NAME = "org.jboss.windup.ui:windup-ui";

    private static final String WINDUP_OLD_VERSION = "2.2.0.Final";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = "org.jboss.forge.furnace.container:cdi"),
                @AddonDependency(name = "org.jboss.windup.utils:windup-utils"),
                @AddonDependency(name = WINDUP_UI_ADDON_NAME),
                @AddonDependency(name = "org.jboss.windup.exec:windup-exec"),
                @AddonDependency(name = "org.jboss.windup.graph:windup-graph"),
                @AddonDependency(name = "org.jboss.windup.rules.apps:windup-rules-java"),
                @AddonDependency(name = "org.jboss.forge.addon:maven"),
                @AddonDependency(name = "org.jboss.forge.addon:addon-manager"),
                @AddonDependency(name = "org.jboss.forge.addon:ui-test-harness"),
    })
    public static AddonArchive getDeployment()
    {
        AddonArchive archive = ShrinkWrap
                    .create(AddonArchive.class)
                    .addBeansXML()
                    .addAsResource(WindupCommandTest.class.getResource(TEST_RULESET_ZIP), TEST_RULESET_ZIP);
        return archive;
    }

    private static String TEST_RULESET_ZIP = "/windup-old-ruleset.zip";

    @Inject
    private DependencyResolver resolver;

    @Inject
    private Addon addon;

    @Inject
    private Furnace furnace;

    @Inject
    private UITestHarness uiTestHarness;

    @Inject
    private RulesetsUpdater updater;

    @Inject
    private DistributionUpdater distUpdater;

    @Inject
    private AddonManager manager;

    @Test
    @Ignore("The current implementation doesn't work as it tampers with addons loaded at the time."
                + " Even if it replaced all Windup addons, it would still fail on Windows as they keep the .jar's locked.")
    public void testUpdateDistribution() throws Exception
    {
        // Download and unzip an old distribution.
        final CoordinateBuilder coords = CoordinateBuilder.create()
                    .setGroupId("org.jboss.windup")
                    .setArtifactId("windup-distribution")
                    .setClassifier("offline")
                    .setVersion("2.2.0.Final")
                    .setPackaging("zip");
        System.out.println("Downloading " + coords + ", may take a while.");
        List<Coordinate> results = resolver.resolveVersions(DependencyQueryBuilder.create(coords));

        File windupDir = OperatingSystemUtils.createTempDir();
        this.updater.extractArtifact(results.get(0), windupDir);
        windupDir = DistributionUpdater.getWindupDistributionSubdir(windupDir);
        Assert.assertTrue(windupDir.exists());
        System.setProperty(PathUtil.WINDUP_HOME, windupDir.getAbsolutePath());

        // Run the upgrader.
        distUpdater.replaceWindupDirectoryWithLatestDistribution();

        // Check the new version.
        String newUiVersion = getInstalledAddonVersion(windupDir.toPath().resolve("addons").toString(), WINDUP_UI_ADDON_NAME);
        Assert.assertTrue(new SingleVersion(newUiVersion).compareTo(new SingleVersion("2.2.0.Final")) > 0);

        // Try to run Windup from there.
        // TODO: I need to set the harness addons directory to the freshly created dir.
        UITestHarness harness = furnace.getAddonRegistry().getServices(UITestHarness.class).get();
        try (CommandController controller = harness.createCommandController(WindupCommand.class))
        {
            controller.initialize();
            controller.setValueFor("input", new File("src/test/resources/test.jar").getAbsolutePath());
            final File resultDir = new File("target/testRunFromUpgraded");
            resultDir.mkdirs();
            controller.setValueFor("output", resultDir.getAbsolutePath());

            Result result = controller.execute();
            Assert.assertTrue(result.getMessage(), !(result instanceof Failed));
        }
        catch (Throwable ex)
        {
            throw new WindupException("Failed running Windup from the upgraded directory: " + ex.getMessage(), ex);
        }
    }

    @Test
    @Ignore("Completely broken now. New Furnace doesn't deal well with setting windup.home like this.")
    public void testUpdateDistributionCommand() throws Exception
    {

        // Unzip the rulesets from a .zip in resources.

        File tempDir = OperatingSystemUtils.createTempDir();
        tempDir.deleteOnExit();
        File extractedPath = new File(tempDir, "extracted-rulesets");
        ZipUtil.unzipFromClassResource(getClass(), WindupUpdateDistributionCommandTest.TEST_RULESET_ZIP, extractedPath);

        String windupDir = extractedPath + "/windup-old-ruleset";
        System.setProperty(PathUtil.WINDUP_RULESETS_DIR_SYSPROP, windupDir);
        File addonsDir = new File(windupDir, "addons");
        addonsDir.mkdirs();

        String currentUiVersion = getInstalledAddonVersion(addon.getRepository().getRootDirectory().getPath(), WINDUP_UI_ADDON_NAME);
        installOldAddonVersion(currentUiVersion); // changeUiAddonVersion(addon.getRepository().getRootDirectory(),
                                                  // currentUiVersion);
        waitForOldWindupUIAddon(furnace);

        boolean rulesetNeedUpdate = updater.rulesetsNeedUpdate();
        Assert.assertTrue(rulesetNeedUpdate);
        try (CommandController controller = uiTestHarness.createCommandController("Windup Update Distribution"))
        {
            try
            {
                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                // Actually runs the command.
                Result result = controller.execute();
                Assert.assertFalse("Windup Update Distribution command should suceed, but it failed.", result instanceof Failed);
                rulesetNeedUpdate = updater.rulesetsNeedUpdate();
                Assert.assertFalse("Ruleset should have already been updated to the latest version and as such should not need another update.",
                            rulesetNeedUpdate);

                checkWindupDirectory(windupDir);
            }
            finally
            {
                FileUtils.deleteDirectory(tempDir);
            }
        }
    }

    private void checkWindupDirectory(String windupDir)
    {
        File addonsHomeNew = new File(windupDir, "addons");
        Assert.assertTrue("Addons folder was not updated sucessfully", addonsHomeNew.exists());
        Assert.assertTrue("Addons folder does not contain enough addons (at least 6)", addonsHomeNew.listFiles().length > 5);

        File binNew = new File(windupDir, "bin");
        Assert.assertTrue("Bin folder was not updated sucessfully", binNew.exists());
        Assert.assertTrue("Binary folder does not contain enough items (at least 2)", binNew.listFiles().length > 1);

        File libNew = new File(windupDir, "lib");
        Assert.assertTrue("Library folder was not updated sucessfully", libNew.exists());
        Assert.assertTrue("Library folder does not contain enough libraries (at least 8)", libNew.listFiles().length > 7);
    }

    private void waitForOldWindupUIAddon(Furnace furnace) throws InterruptedException
    {
        Addon addon = furnace.getAddonRegistry().getAddon(AddonId.from(WINDUP_UI_ADDON_NAME, WINDUP_OLD_VERSION));
        Addons.waitUntilStarted(addon);
        do
        {
            // We need to wait till furnace will process all the information changed in the directories.
            Thread.sleep(500);
        }
        while (furnace.getAddonRegistry().getServices(WindupUpdateDistributionCommand.class).isUnsatisfied());
    }

    /**
     * Uninstalls the current addon and installs the other one. This fails because we would need to replace all the
     * dependencies as well, effectively, the whole Windup.
     */
    private void installOldAddonVersion(String currentUiVersion)
    {
        RemoveRequest remove = manager.remove(AddonId.from(WINDUP_UI_ADDON_NAME, currentUiVersion));
        remove.perform();
        final AddonId olderAddonId = AddonId.from(WINDUP_UI_ADDON_NAME, WINDUP_OLD_VERSION);
        log.info("Downgrading to " + olderAddonId + ". This may take a while to download.");
        InstallRequest install = manager.install(olderAddonId);
        install.perform();
    }

    /**
     * Changes the org-jboss-windup-ui-windup-ui-* dir name to old version and rewrites the version in installed.xml. It
     * is a hack to fool Furnace into thinking a new addon was installed.
     */
    private void changeUiAddonVersion(File addonsDir, String currentUiVersion)
    {
        File currentAddonDir = new File(addonsDir,
                    "org-jboss-windup-ui-windup-ui-" + currentUiVersion.replaceAll("\\.", "-"));
        File olderVersionAddonDir = new File(addonsDir,
                    "org-jboss-windup-ui-windup-ui-" + WINDUP_OLD_VERSION.replaceAll("\\.", "-"));
        olderVersionAddonDir.mkdirs();

        log.warning("Replacing the addon: \n  " + currentAddonDir + "\n  " + olderVersionAddonDir);
        try
        {
            FileUtils.copyDirectory(currentAddonDir, olderVersionAddonDir);
            changeUiAddonVersionInInstallXml(addonsDir.getPath());
            FileUtils.deleteDirectory(currentAddonDir);
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Failed replacing the addon: ", ex);
        }
    }

    /**
     * Reads the version of the windup-ui addon from addons/installed.xml .
     */
    private String getInstalledAddonVersion(String addonsRootDir, String addonName)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try
        {
            String oldUiAddonVersion = "";
            File installedXml = new File(addonsRootDir, "installed.xml");
            if (!installedXml.exists())
                throw new WindupException("installed.xml doesn't exist: " + installedXml.getAbsolutePath());

            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(installedXml);
            // this does not work properly
            Element documentElement = doc.getDocumentElement();
            NodeList childNodes = documentElement.getElementsByTagName("addon");
            for (int i = 0; i <= childNodes.getLength() - 1; i++)
            {
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("addon"))
                {
                    String addonNameAttr = item.getAttribute("name");
                    if (addonNameAttr.equals(addonName))
                    {
                        oldUiAddonVersion = item.getAttribute("version");
                        return oldUiAddonVersion;
                    }
                }
            }
        }
        catch (ParserConfigurationException ex)
        {
            throw new RuntimeException("Failed parsing installed.xml: " + ex.getMessage(), ex);
        }
        catch (WindupException ex)
        {
            throw ex;
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Unknown exception: " + ex.getMessage(), ex);
        }
        return null;
    }

    /**
     * Writes the version WINDUP_OLD_VERSION into installed.xml in given dir.
     */
    private void changeUiAddonVersionInInstallXml(String homeAddonsDirPath)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            File installedXml = new File(homeAddonsDirPath, "installed.xml");
            Document doc = dBuilder.parse(installedXml);
            // this does not work properly
            Element documentElement = doc.getDocumentElement();
            NodeList childNodes = documentElement.getElementsByTagName("addon");
            for (int i = 0; i <= childNodes.getLength() - 1; i++)
            {
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("addon"))
                {
                    String addonName = item.getAttribute("name");
                    if (addonName.equals(WINDUP_UI_ADDON_NAME))
                    {
                        item.setAttribute("version", WINDUP_OLD_VERSION);
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource source = new DOMSource(doc);
                        StreamResult result = new StreamResult(installedXml);
                        transformer.transform(source, result);
                        return;
                    }
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            System.out.println("Why?");
        }
        return;
    }

}