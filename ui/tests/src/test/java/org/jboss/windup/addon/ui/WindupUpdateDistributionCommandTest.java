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
import java.util.Set;

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
import org.jboss.forge.furnace.util.OperatingSystemUtils;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.windup.ui.RulesetUpdateChecker;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Test distribution update by faking the old installation by downgrading windup-ui addon (changing installed.xml and windup-ui addon folder name).
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 *
 */
@RunWith(Arquillian.class)
public class WindupUpdateDistributionCommandTest
{
    private static String WINDUP_OLD_VERSION = "2.0.0.Final";
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
                    .addAsResource(WindupUpdateDistributionCommandTest.class.getResource(TEST_OLD_WINDUP), TEST_OLD_WINDUP);
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

    /**
     * Changes version of the windup-ui addon and then ask to update and checks that the .update is correctly prepared in the $WINDUP_HOME directory.
     * @throws Exception
     */
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
        do{
          //we need to wait till furnace will process all the information changed in the directories
          Thread.sleep(500);
        } while(furnace.getAddonRegistry().getServices("org.jboss.windup.ui.WindupUpdateDistributionCommand").isUnsatisfied());
        
        boolean rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver);
        Assert.assertTrue(rulesetNeedUpdate);
        try (CommandController controller = uiTestHarness.createCommandController("Windup Update Distribution"))
        {
            try
            {
                controller.initialize();
                Assert.assertTrue(controller.isEnabled());
                Assert.assertFalse(new File(windupHome + "/.update").exists());
                Result result = controller.execute();
                Assert.assertFalse("Windup Update Distribution command should suceed, but it failed.",result instanceof Failed);
                Assert.assertTrue(".update folder should have been placed in WINDUP_HOME/.update already, but it is not there.",new File(windupHome + "/.update").exists());
                String pomXmlPath = "";
                if(new File(windupHome + "/.update/rules/migration-core").exists()) {
                    pomXmlPath= windupHome + "/.update/rules/migration-core/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.xml";
                } else {
                    pomXmlPath= windupHome + "/.update/rules/META-INF/maven/org.jboss.windup.rules/windup-rulesets/pom.xml";
                }
                rulesetNeedUpdate = RulesetUpdateChecker.rulesetNeedUpdate(resolver,pomXmlPath);
                Assert.assertFalse("Ruleset should have already been downloaded with the most updated version.",rulesetNeedUpdate);
                File addonsHomeNew = new File(windupHome + "/.update/addons");
                File binNew = new File(windupHome + "/.update/bin");
                File libNew = new File(windupHome + "/.update/lib");
                Assert.assertTrue(".update/addons folder was not updated sucessfully, it should exist now",addonsHomeNew.exists());
                Assert.assertTrue(".update/bin folder was not updated sucessfully, it should exist now",binNew.exists());
                Assert.assertTrue(".update/lib folder was not updated sucessfully, it should exist now",libNew.exists());
                Assert.assertTrue(".update/bin folder does not contain enough items (at least 2)",binNew.listFiles().length > 1);
                Assert.assertTrue(".update/lib folder does not contain enough libraries (at least 8)",libNew.listFiles().length > 7);
                Assert.assertTrue(".update/addons folder does not contain enough addons (at least 6)",addonsHomeNew.listFiles().length > 5);
            }
            finally
            {
                FileUtils.deleteDirectory(tempDir);
            }
        }
    }
    

    /**
     * Wait for the windup-ui addon to start.
     * @param furnace
     */
    private void waitForWindupUIAddon(Furnace furnace) {
        Set<Addon> addons = furnace.getAddonRegistry().getAddons();
        for(Addon addon : addons) {
            if(addon.getId().getVersion().toString().equals(WINDUP_OLD_VERSION) && addon.getId().getName().contains("windup-ui")) {
                while(!addon.getStatus().isStarted()) {
                    try
                    {
                        Thread.sleep(100);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /**
     * This is because UI-Addon contains addon version check to know if it needs to update. 
     * After changing the version to older, the check will return that it needs an update.
     * @param homeAddonsDir
     * @param uiPreviousVersion
     */
    private void changeUiAddonDirectoryToBeOlder(File homeAddonsDir, String uiPreviousVersion)
    {
        File uiAddonDirectory = new File(homeAddonsDir.getAbsolutePath() + "/org-jboss-windup-ui-windup-ui-"
                    + uiPreviousVersion.replaceAll("\\.", "-"));
        File olderVersionAddon = new File(homeAddonsDir.getAbsolutePath() + "/org-jboss-windup-ui-windup-ui-" + WINDUP_OLD_VERSION.replaceAll("\\.", "-"));
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

    /**
     * Gets the version of org.jboss.windup.ui:windup-ui, information is taken from installed.xml file.
     * @param homeAddonsDirPath
     * @return
     */
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
                    if (addonName.equals("org.jboss.windup.ui:windup-ui"))
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
                    if (addonName.equals("org.jboss.windup.ui:windup-ui"))
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