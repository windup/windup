package org.jboss.windup.decompiler.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

/**
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ZipUtil
{
    /**
     * Unzip the given archive into the specified directory.
     */
    public static void unzip(File inputFile, File outputDir) throws IOException
    {
        unzipWithFilter(inputFile, outputDir, null);
    }

    /**
     * Unzip the given archive into the specified directory, using given filter. Directories are created only if needed.
     */
    public static void unzipWithFilter(File inputFile, File outputDir, Filter<ZipEntry> filter) throws IOException
    {
        try (ZipFile zipFile = new ZipFile(inputFile))
        {
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
            while (entryEnum.hasMoreElements())
            {
                ZipEntry entry = entryEnum.nextElement();
                String entryName = entry.getName();
                File destFile = new File(outputDir, entryName);
                if (entry.isDirectory())
                    continue;

                if (filter != null)
                {
                    final Filter.Result res = filter.decide(entry);
                    switch (res)
                    {
                    case STOP:
                        return;
                    case REJECT:
                        continue;
                    }
                }

                File parentDir = destFile.getParentFile();
                if (!parentDir.isDirectory() && !parentDir.mkdirs())
                    throw new RuntimeException("Unable to create directory: " + parentDir.getAbsolutePath());

                try (InputStream zipInputStream = zipFile.getInputStream(entry))
                {
                    try (FileOutputStream outputStream = new FileOutputStream(destFile))
                    {
                        IOUtils.copy(zipInputStream, outputStream);
                    }
                }

                if (filter != null)
                {
                    final Filter.Result res = filter.decide(entry);
                    switch (res)
                    {
                    case ACCEPT_STOP:
                        return;
                    }
                }
            }
        }
    }

}
