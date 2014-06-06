package org.jboss.windup.ext.groovy;

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

public class FurnaceGroovyRuleScanner
{
    private static final Logger LOG = LoggerFactory.getLogger(FurnaceGroovyRuleScanner.class);

    @Inject
    private Furnace furnace;

    public Iterable<URL> scan()
    {
        List<URL> discoveredRuleFiles = new ArrayList<>();

        for (Addon addon : furnace.getAddonRegistry().getAddons(AddonFilters.allStarted()))
        {
            List<String> discoveredRuleFileNames = new ArrayList<>();
            List<File> addonResources = addon.getRepository().getAddonResources(addon.getId());
            for (File addonFile : addonResources)
            {
                if (addonFile.isDirectory())
                {
                    handleDirectory(addonFile, null, discoveredRuleFileNames);
                }
                else
                {
                    handleArchiveByFile(addonFile, discoveredRuleFileNames);
                }
            }

            for (String discoveredRuleFileName : discoveredRuleFileNames)
            {
                URL ruleFile = addon.getClassLoader().getResource(discoveredRuleFileName);
                if (ruleFile != null)
                    discoveredRuleFiles.add(ruleFile);
            }
        }
        return discoveredRuleFiles;
    }

    private void handleArchiveByFile(File file, List<String> discoveredFiles)
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
                handle(name, new URL(archiveUrl + name), discoveredFiles);
            }
            zip.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException("Error handling file " + file, e);
        }
    }

    private void handleDirectory(File file, String path, List<String> discoveredFiles)
    {
        for (File child : file.listFiles())
        {
            String newPath = (path == null) ? child.getName() : (path + '/' + child.getName());

            if (child.isDirectory())
            {
                handleDirectory(child, newPath, discoveredFiles);
            }
            else
            {
                try
                {
                    handle(newPath, child.toURI().toURL(), discoveredFiles);
                }
                catch (MalformedURLException e)
                {
                    LOG.error("Error loading file: " + newPath, e);
                }
            }
        }
    }

    protected void handle(String name, URL url, List<String> discoveredFiles)
    {
        if (name.endsWith(".wrl") || name.endsWith(".windup.groovy")) // TODO handlers should be extensible
        {
            discoveredFiles.add(name);
        }
    }

}
