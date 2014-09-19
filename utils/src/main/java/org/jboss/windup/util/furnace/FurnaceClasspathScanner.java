package org.jboss.windup.util.furnace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.lang.StringUtils;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.Service;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.util.AddonFilters;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author jsightler
 */
public class FurnaceClasspathScanner implements Service
{
    private static final Logger LOG = Logger.getLogger(FurnaceClasspathScanner.class.getName());
    private final Furnace furnace;

    public FurnaceClasspathScanner()
    {
        furnace = SimpleContainer.getFurnace(FurnaceClasspathScanner.class.getClassLoader());
    }

    public List<URL> scan(String fileExtension)
    {
        return scan(new FurnaceScannerFileExtensionFilenameFilter(fileExtension));
    }

    /**
     * Scans all Forge addons for files accepted by given filter.
     */
    public List<URL> scan(FurnaceScannerFilenameFilter filter)
    {
        List<URL> discoveredURLs = new ArrayList<>();

        // For each Forge addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredFileNames = new ArrayList<>();
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            // For each addon resource - i.e. addon's jar or directory.
            for (File addonFile : addonResources)
            {
                // Addon in a directory
                if (addonFile.isDirectory())
                    handleDirectory(filter, addonFile, null, discoveredFileNames);

                // Addon in a .jar file.
                else
                    handleArchiveByFile(filter, addonFile, discoveredFileNames);
            }

            for (String discoveredFileName : discoveredFileNames)
            {
                URL ruleFile = addon.getClassLoader().getResource(discoveredFileName);
                if (ruleFile != null)
                    discoveredURLs.add(ruleFile);
            }
        }
        return discoveredURLs;
    }

    
    /**
     * Scans all Forge addons for classes accepted by given filter.
     * 
     * TODO: Needs refactoring - scan() is almost the same.
     */
    public Iterable<Class<?>> scanClasses(FurnaceScannerFilenameFilter filter)
    {
        List<Class<?>> discoveredClasses = new ArrayList<>();

        // For each Forge addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredFileNames = new ArrayList<>();
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            // For each addon resource - i.e. addon's jar or directory.
            for (File addonFile : addonResources)
            {
                // Addon in a directory
                if (addonFile.isDirectory())
                    handleDirectory(filter, addonFile, null, discoveredFileNames);

                // Addon in a .jar file.
                else
                    handleArchiveByFile(filter, addonFile, discoveredFileNames);
            }

            // Then try to load the classes.
            for (String discoveredFilename : discoveredFileNames)
            {
                String discoveredClassName = filepathToClassname(discoveredFilename);
                try
                {
                    Class<?> clazz = addon.getClassLoader().loadClass(discoveredClassName);
                    discoveredClasses.add(clazz);
                }
                catch (ClassNotFoundException cnfe)
                {
                    LOG.log(Level.WARNING, "Failed to load class for name: " + discoveredClassName);
                }
            }
        }
        return discoveredClasses;
    }

    
    /**
     * Scans given archive for files passing given filter.
     */
    private static void handleArchiveByFile(FurnaceScannerFilenameFilter filter, File archive, List<String> discoveredFiles)
    {
        try
        {
            String archiveUrl = "jar:" + archive.toURI().toURL().toExternalForm() + "!/";
            ZipFile zip = new ZipFile(archive);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                String subPath = entry.getName();
                handle(filter, subPath, new URL(archiveUrl + subPath), discoveredFiles);
            }
            zip.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error handling file " + archive, e);
        }
    }

    private static void handleDirectory(FurnaceScannerFilenameFilter filter, File file, String curPath,
                List<String> discoveredFiles)
    {
        for (File child : file.listFiles())
        {
            String subPath = (curPath == null) ? child.getName() : (curPath + '/' + child.getName());

            if (child.isDirectory())
            {
                handleDirectory(filter, child, subPath, discoveredFiles);
            }
            else
            {
                try
                {
                    handle(filter, subPath, child.toURI().toURL(), discoveredFiles);
                }
                catch (MalformedURLException e)
                {
                    LOG.log(Level.SEVERE, "Error loading file: " + subPath, e);
                }
            }
        }
    }

    private static void handle(FurnaceScannerFilenameFilter filter, String subPath, URL url, List<String> discoveredFiles)
    {
        if (filter.accept(subPath))
        {
            discoveredFiles.add(subPath);
        }
    }

    public static String filepathToClassname(String filename)
    {
        return StringUtils.removeEndIgnoreCase(filename, ".class").replace('/', '.').replace('\\', '.');
    }
}
