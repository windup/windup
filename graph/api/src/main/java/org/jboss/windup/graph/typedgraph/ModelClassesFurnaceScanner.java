package org.jboss.windup.graph.typedgraph;

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
 *  Scans all Furnace addons for classes named *Model.class.
 */
class ModelClassesFurnaceScanner
{
    private static final Logger LOG = LoggerFactory.getLogger(ModelClassesFurnaceScanner.class);

    @Inject
    private Furnace furnace;

    public List<Class<?>> scan()
    {
        List<Class<?>> discoveredClasses = new ArrayList<>();

        // For each addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            // Scan for class files, derive class names.
            List<String> discoveredClassNames = new ArrayList<>();
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            for (File addonFile : addonResources)
            {
                if (addonFile.isDirectory())
                {
                    handleDirectory(addonFile, null, discoveredClassNames);
                }
                else
                {
                    handleArchiveByFile(addonFile, discoveredClassNames);
                }
            }
            
            // Then try to load the classes.
            for (String discoveredClassName : discoveredClassNames)
            {
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

    private void handleArchiveByFile(File file, List<String> discoveredClasses)
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
                handle(name, new URL(archiveUrl + name), discoveredClasses);
            }
            zip.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error handling file " + file, e);
        }
    }

    private void handleDirectory(File file, String path, List<String> discoveredClasses)
    {
        for (File child : file.listFiles())
        {
            String newPath = (path == null) ? child.getName() : (path + '/' + child.getName());

            if (child.isDirectory())
            {
                handleDirectory(child, newPath, discoveredClasses);
            }
            else
            {
                try
                {
                    handle(newPath, child.toURI().toURL(), discoveredClasses);
                }
                catch (MalformedURLException e)
                {
                    LOG.error("Error loading file: " + newPath, e);
                }
            }
        }
    }

    protected void handle(String name, URL url, List<String> discoveredClasses)
    {
        if (name.endsWith("Model.class"))
        {
            String className = filenameToClassname(name);
            discoveredClasses.add(className);
        }
    }

    /**
     * Convert a path to a class file to a class name
     */
    public static String filenameToClassname(String filename)
    {
        return filename.substring(0, filename.lastIndexOf(".class")).replace('/', '.').replace('\\', '.');
    }
}
