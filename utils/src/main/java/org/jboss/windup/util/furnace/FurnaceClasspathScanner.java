package org.jboss.windup.util.furnace;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.DirectoryWalker;
import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.addons.Addon;
import org.jboss.forge.furnace.container.simple.lifecycle.SimpleContainer;
import org.jboss.forge.furnace.util.AddonFilters;
import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.util.WindupPathUtil;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author jsightler
 * @author Ondrej Zizka
 */
public class FurnaceClasspathScanner
{
    private static final Logger LOG = Logger.getLogger(FurnaceClasspathScanner.class.getName());
    private final Furnace furnace;

    public FurnaceClasspathScanner()
    {
        furnace = SimpleContainer.getFurnace(FurnaceClasspathScanner.class.getClassLoader());
    }

    public List<URL> scan(String fileExtension)
    {
        return scan(new FileExtensionFilter(fileExtension));
    }

    /**
     * Scans all Forge addons for files accepted by given filter, and return them as a map (from Addon to URL list)
     */
    public Map<Addon, List<URL>> scanForAddonMap(Predicate<String> filter)
    {
        Map<Addon, List<URL>> result = new IdentityHashMap<>();

        // For each Forge addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> filteredResourcePaths = filterAddonResources(addon, filter);

            List<URL> discoveredURLs = new ArrayList<>();
            for (String filePath : filteredResourcePaths)
            {
                URL ruleFile = addon.getClassLoader().getResource(filePath);
                if (ruleFile != null)
                    discoveredURLs.add(ruleFile);
            }
            result.put(addon, discoveredURLs);
        }
        return result;
    }

    /**
     * Scans all Forge addons for files accepted by given filter.
     */
    public List<URL> scan(Predicate<String> filter)
    {
        List<URL> discoveredURLs = new ArrayList<>(128);

        // For each Forge addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> filteredResourcePaths = filterAddonResources(addon, filter);
            for (String filePath : filteredResourcePaths)
            {
                URL ruleFile = addon.getClassLoader().getResource(filePath);
                if (ruleFile != null)
                    discoveredURLs.add(ruleFile);
            }
        }
        return discoveredURLs;
    }

    /**
     * Scans all Forge addons for classes accepted by given filter.
     *
     * TODO: Could be refactored - scan() is almost the same.
     */
    public List<Class<?>> scanClasses(Predicate<String> filter)
    {
        List<Class<?>> discoveredClasses = new ArrayList<>(128);

        // For each Forge addon...
        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredFileNames = filterAddonResources(addon, filter);

            // Then try to load the classes.
            for (String discoveredFilename : discoveredFileNames)
            {
                String clsName = WindupPathUtil.classFilePathToClassname(discoveredFilename);
                try
                {
                    Class<?> clazz = addon.getClassLoader().loadClass(clsName);
                    discoveredClasses.add(clazz);
                }
                catch (ClassNotFoundException ex)
                {
                    LOG.log(Level.WARNING, "Failed to load class for name '" + clsName + "':\n" + ex.getMessage(), ex);
                }
            }
        }
        return discoveredClasses;
    }

    /**
     * Returns a list of files in given addon passing given filter.
     */
    public List<String> filterAddonResources(Addon addon, Predicate<String> filter)
    {
        List<String> discoveredFileNames = new ArrayList<>();
        List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
        for (File addonFile : addonResources)
        {
            if (addonFile.isDirectory())
                handleDirectory(filter, addonFile, discoveredFileNames);
            else
                handleArchiveByFile(filter, addonFile, discoveredFileNames);
        }
        return discoveredFileNames;
    }

    /**
     * Scans given archive for files passing given filter, adds the results into given list.
     */
    private void handleArchiveByFile(Predicate<String> filter, File archive, List<String> discoveredFiles)
    {
        try
        {
            ZipFile zip = new ZipFile(archive);
            Enumeration<? extends ZipEntry> entries = zip.entries();

            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (filter.accept(name))
                    discoveredFiles.add(name);
            }
            zip.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error handling file " + archive, e);
        }
    }

    /**
     * Scans given directory for files passing given filter, adds the results into given list.
     */
    private void handleDirectory(final Predicate<String> filter, final File rootDir, final List<String> discoveredFiles)
    {
        try
        {
            new DirectoryWalker<String>()
            {
                private Path startDir;

                public void walk() throws IOException
                {
                    this.startDir = rootDir.toPath();
                    this.walk(rootDir, discoveredFiles);
                }

                @Override
                protected void handleFile(File file, int depth, Collection<String> discoveredFiles) throws IOException
                {
                    String newPath = startDir.relativize(file.toPath()).toString();
                    if (filter.accept(newPath))
                        discoveredFiles.add(newPath);
                }

            }.walk();
        }
        catch (IOException ex)
        {
            LOG.log(Level.SEVERE, "Error reading Furnace addon directory", ex);
        }
    }
}
