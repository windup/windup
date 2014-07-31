package org.jboss.windup.util.furnace;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.inject.Inject;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.util.AddonFilters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author jsightler
 * 
 * TODO: Refactor so scan() and scanForClasses() reuse shared code.
 */
public class FurnaceClasspathScanner
{
    private static final Logger LOG = LoggerFactory.getLogger(FurnaceClasspathScanner.class);

    @Inject
    private Furnace furnace;

    
    public Iterable<URL> scan(String fileExtension)
    {
        return scan(new FurnaceScannerFileExtensionFilenameFilter(fileExtension));
    }

    public Iterable<URL> scan(FurnaceScannerFilenameFilter filter)
    {
        List<URL> discoveredURLs = new ArrayList<>(128);

        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredFileNames = new ArrayList<>(32);
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            for (File addonFile : addonResources)
            {
                if (addonFile.isDirectory())
                {
                    handleDirectory(filter, addonFile, null, discoveredFileNames);
                }
                else
                {
                    handleArchiveByFile(filter, addonFile, discoveredFileNames);
                }
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
     *  The same as scan(), only loads the classes.
     */
    public Iterable<Class<?>> scanClasses(FurnaceScannerFilenameFilter filter)
    {
        List<Class<?>> discoveredClasses = new ArrayList<>(128);

        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredFileNames = new ArrayList<>(32);
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            for (File addonFile : addonResources)
            {
                if (addonFile.isDirectory())
                {
                    handleDirectory(filter, addonFile, null, discoveredFileNames);
                }
                else
                {
                    handleArchiveByFile(filter, addonFile, discoveredFileNames);
                }
            }

            // Then try to load the classes.
            for (String discoveredFilename : discoveredFileNames)
            {
                String discoveredClassName = filenameToClassname(discoveredFilename);
                try
                {
                    Class<?> clazz = addon.getClassLoader().loadClass(discoveredClassName);
                    discoveredClasses.add(clazz);
                }
                catch (ClassNotFoundException cnfe)
                {
                    LOG.warn("Failed to load class for name: " + discoveredClassName);
                }
            }
        }
        return discoveredClasses;
    }

    private void handleArchiveByFile(FurnaceScannerFilenameFilter filter, File file, List<String> discoveredFiles)
    {
        try
        {
            String archiveUrl = "jar:" + file.toURI().toURL().toExternalForm() + "!/";
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                handle(filter, name, new URL(archiveUrl + name), discoveredFiles);
            }
            zip.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error handling file " + file, e);
        }
    }

    private void handleDirectory(FurnaceScannerFilenameFilter filter, File file, String path,
                List<String> discoveredFiles)
    {
        for (File child : file.listFiles())
        {
            String newPath = (path == null) ? child.getName() : (path + '/' + child.getName());

            if (child.isDirectory())
            {
                handleDirectory(filter, child, newPath, discoveredFiles);
            }
            else
            {
                try
                {
                    handle(filter, newPath, child.toURI().toURL(), discoveredFiles);
                }
                catch (MalformedURLException e)
                {
                    LOG.error("Error loading file: " + newPath, e);
                }
            }
        }
    }

    private void handle(FurnaceScannerFilenameFilter filter, String name, URL url, List<String> discoveredFiles)
    {
        if (filter.accept(name))
        {
            discoveredFiles.add(name);
        }
    }

    private String filenameToClassname(String filename)
    {
        return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
    }
}
