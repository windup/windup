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
 */
public class ZipUtil
{
    /**
     * Unzip the given file into the specified directory.
     */
    public static void unzip(File inputFile, File outputDir) throws IOException
    {
        try (ZipFile zipFile = new ZipFile(inputFile))
        {
            Enumeration<? extends ZipEntry> entryEnum = zipFile.entries();
            while (entryEnum.hasMoreElements())
            {
                ZipEntry entry = entryEnum.nextElement();
                String entryName = entry.getName();
                File destFile = new File(outputDir, entryName);
                if (!entry.isDirectory())
                {
                    File parentDir = destFile.getParentFile();
                    if (!parentDir.isDirectory() && !parentDir.mkdirs())
                    {
                        throw new RuntimeException("Unable to create directory: " + parentDir.getAbsolutePath());
                    }
                    try (InputStream zipInputStream = zipFile.getInputStream(entry))
                    {
                        try (FileOutputStream outputStream = new FileOutputStream(destFile))
                        {
                            IOUtils.copy(zipInputStream, outputStream);
                        }
                    }
                }
            }
        }
    }
}