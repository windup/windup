package org.jboss.windup.addon.ui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.jboss.forge.arquillian.AddonDependencies;
import org.jboss.forge.arquillian.AddonDependency;
import org.jboss.forge.arquillian.archive.AddonArchive;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.addons.AddonId;
import org.jboss.forge.furnace.util.Addons;
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ui.RulesetUpdateChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@RunWith(Arquillian.class)
public class WindupUpdateDistributionCommandTest
{
    private static final String WINDUP_UI_ADDON_NAME = "org.jboss.windup.ui:windup-ui";

    private static String WINDUP_OLD_VERSION = "2.0.0.Final";

    @Deployment
    @AddonDependencies({
                @AddonDependency(name = WINDUP_UI_ADDON_NAME),
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
    private DependencyResolver resolver;

    @Inject
    private Addon addon;

    @Inject
    private Furnace furnace;

    @Inject
    private UITestHarness uiTestHarness;

    @Test
    public void testUpdateDistributionCommand() throws Exception
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

        ZipFile zipFile = new ZipFile(inputFile.getAbsolutePath());
        String extractedFolderPath = tempDir.getAbsolutePath() + "/extracted-windup";
        new File(extractedFolderPath).mkdirs();
        zipFile.extractAll(extractedFolderPath);
        String windupHome = extractedFolderPath + "/windup-old-ruleset";
        System.setProperty("windup.home", windupHome);
        File homeAddonsDir = new File(windupHome + "/addons");
        Assert.assertTrue(homeAddonsDir.exists());
        Assert.assertTrue(homeAddonsDir.listFiles().length == 0);
        String uiPreviousVersion = getPreviousVersion(addon.getRepository().getRootDirectory().getPath());
        changeUiAddonDirectoryToBeOlder(addon.getRepository().getRootDirectory(), uiPreviousVersion);
        waitForWindupUIAddon(furnace);
        do
        {
            // we need to wait till furnace will process all the information changed in the directories
            Thread.sleep(500);
        }
        while (furnace.getAddonRegistry().getServices("org.jboss.windup.ui.WindupUpdateDistributionCommand").isUnsatisfied());

        boolean rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver);
        Assert.assertTrue(rulesetNeedUpdate);
        try (CommandController controller = uiTestHarness.createCommandController("Windup Update Distribution"))
        {
            try
            {
                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                Result result = controller.execute();
                Assert.assertFalse("Windup Update Distribution command should suceed, but it failed.", result instanceof Failed);
                rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver);
                Assert.assertFalse("Ruleset should have already been updated to the latest version and as such should not need another update.",
                            rulesetNeedUpdate);
                File addonsHomeNew = new File(windupHome + "/addons");
                File binNew = new File(windupHome + "/bin");
                File libNew = new File(windupHome + "/lib");
                Assert.assertTrue("Addons folder was not updated sucessfully", addonsHomeNew.exists());
                Assert.assertTrue("Bin folder was not updated sucessfully", binNew.exists());
                Assert.assertTrue("Library folder was not updated sucessfully", libNew.exists());
                Assert.assertTrue("Binary folder does not contain enough items (at least 2)", binNew.listFiles().length > 1);
                Assert.assertTrue("Library folder does not contain enough libraries (at least 8)", libNew.listFiles().length > 7);
                Assert.assertTrue("Addons folder does not contain enough addons (at least 6)", addonsHomeNew.listFiles().length > 5);
            }
            finally
            {
                FileUtils.deleteDirectory(tempDir);
            }
        }
    }

    private void waitForWindupUIAddon(Furnace furnace)
    {
        Addon addon = furnace.getAddonRegistry().getAddon(AddonId.from(WINDUP_UI_ADDON_NAME, WINDUP_OLD_VERSION));
        Addons.waitUntilStarted(addon);
    }

    private void changeUiAddonDirectoryToBeOlder(File homeAddonsDir, String uiPreviousVersion)
    {
        File uiAddonDirectory = new File(homeAddonsDir.getAbsolutePath() + "/org-jboss-windup-ui-windup-ui-"
                    + uiPreviousVersion.replaceAll("\\.", "-"));
        File olderVersionAddon = new File(homeAddonsDir.getAbsolutePath() + "/org-jboss-windup-ui-windup-ui-"
                    + WINDUP_OLD_VERSION.replaceAll("\\.", "-"));
        if (!olderVersionAddon.exists())
        {
            olderVersionAddon.mkdir();
        }
        try
        {
            FileUtils.copyDirectory(uiAddonDirectory, olderVersionAddon);
            makeUiAddonVersionOlder(homeAddonsDir.getPath());
            deleteWholeDirectory(uiAddonDirectory);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String getPreviousVersion(String homeAddonsDirPath)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            String oldUiAddonVersion = "";
            File installedXml = new File(homeAddonsDirPath + "/installed.xml");
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
                        oldUiAddonVersion = item.getAttribute("version");
                        return oldUiAddonVersion;
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
        return null;
    }

    private void makeUiAddonVersionOlder(String homeAddonsDirPath)
    {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try
        {
            dBuilder = dbFactory.newDocumentBuilder();
            File installedXml = new File(homeAddonsDirPath + "/installed.xml");
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

    private void deleteWholeDirectory(File directory) throws IOException
    {
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>()
        {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException
            {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir,
                        IOException exc) throws IOException
            {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

}