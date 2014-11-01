package org.jboss.windup.gui.services;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.windup.util.Logging;
import org.jboss.windup.util.WindupPathUtil;
import org.jboss.windup.util.ZipUtil;

/**
 * Provides methods for scanning an input path (for packages or just for contained files).
 * 
 * @author jsightler
 */
public class InputScanner
{
    private static Logger LOG = Logging.get(InputScanner.class);

    private final Path path;

    public InputScanner(Path path)
    {
        this.path = path;
    }

    /**
     * Recursively scan the provided path and return a list of all Java packages contained therein.
     */
    public Collection<String> findPackages()
    {
        List<String> paths = findPaths(path, true);
        Set<String> results = new HashSet<>();
        for (String path : paths)
        {
            if (path.endsWith(".java") || path.endsWith(".class"))
            {
                String packageName = WindupPathUtil.pathToPackagename(path);
                results.add(packageName);
            }
        }

        List<String> resultList = new ArrayList<>();
        resultList.addAll(results);
        sortAlphabetically(resultList);
        return resultList;
    }

    /**
     * Find all paths within the given file (or folder).
     */
    public Collection<String> findPaths()
    {
        List<String> paths = findPaths(path, false);
        sortAlphabetically(paths);
        return paths;
    }

    private void sortAlphabetically(List<String> list)
    {
        Collections.sort(list, new Comparator<String>()
        {
            @Override
            public int compare(String o1, String o2)
            {
                return o1.compareTo(o2);
            }
        });
    }

    private List<String> findPaths(Path path, boolean relativeOnly)
    {
        List<String> results = new ArrayList<>();
        results.add(path.normalize().toAbsolutePath().toString());
        if (Files.isDirectory(path))
        {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path))
            {
                for (Path child : directoryStream)
                {
                    results.addAll(findPaths(child, relativeOnly));
                }
            }
            catch (IOException e)
            {
                LOG.warning("Could not read file: " + path + " due to: " + e.getMessage());
            }
        }
        else if (Files.isRegularFile(path) && ZipUtil.endsWithZipExtension(path.toString()))
        {
            results.addAll(scanZipFile(path, relativeOnly));
        }
        return results;
    }

    private List<String> scanZipFile(Path zipFilePath, boolean relativeOnly)
    {
        try
        {
            try (InputStream is = new FileInputStream(zipFilePath.toFile()))
            {
                return scanZipFile(zipFilePath.normalize().toString(), is, relativeOnly);
            }
        }
        catch (IOException e)
        {
            LOG.warning("Could not read file: " + zipFilePath + " due to: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<String> scanZipFile(String parentPath, InputStream is, boolean relativeOnly)
    {
        try
        {
            ZipInputStream zis = new ZipInputStream(is);
            ZipEntry entry;
            List<String> results = new ArrayList<>();
            while ((entry = zis.getNextEntry()) != null)
            {
                String fullPath = parentPath + "/" + entry.getName();
                results.add(relativeOnly ? entry.getName() : fullPath);
                if (!entry.isDirectory() && ZipUtil.endsWithZipExtension(entry.getName()))
                {
                    results.addAll(scanZipFile(fullPath, zis, relativeOnly));
                }
            }
            return results;
        }
        catch (IOException e)
        {
            LOG.warning("Could not read file: " + parentPath + " due to: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
