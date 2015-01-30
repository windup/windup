package org.jboss.windup.util;

import java.io.File;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class Checks
{

    // --- Input validating methods. ---

    public static void checkFileToBeRead(File archive, String fileDesc) throws IllegalArgumentException
    {
        if (archive == null)
            throw new IllegalArgumentException(fileDesc + " must not be null.");
        if (!archive.exists())
            throw new IllegalArgumentException(fileDesc + " does not exist: " + archive.getAbsolutePath());
        if (archive.isDirectory())
            throw new IllegalArgumentException(fileDesc + " is a directory, expected a file: " + archive.getPath());
    }

    public static void checkDirectoryToBeFilled(File outputDir, String dirDesc) throws IllegalArgumentException
    {
        if (outputDir == null)
            throw new IllegalArgumentException(dirDesc + " must not be null.");
        if (outputDir.exists() && !outputDir.isDirectory())
            throw new IllegalArgumentException(dirDesc + " is a file, expected a directory: " + outputDir.getAbsolutePath());
    }

    public static void checkDirectoryToBeRead(File rootDir, String dirDesc)
    {
        if (rootDir == null)
            throw new IllegalArgumentException(dirDesc + " must not be null.");
        if (!rootDir.exists())
            throw new IllegalArgumentException(dirDesc + " does not exist: " + rootDir.getPath());
        if (!rootDir.isDirectory())
            throw new IllegalArgumentException(dirDesc + " is a file, expected a directory: " + rootDir.getPath());
    }

    /**
     * Throws if the given file is null, is not a file or directory, or is an empty directory.
     */
    public static void checkFileOrDirectoryToBeRead(File fileOrDir, String fileDesc)
    {
        if (fileOrDir == null)
            throw new IllegalArgumentException(fileDesc + " must not be null.");
        if (!fileOrDir.exists())
            throw new IllegalArgumentException(fileDesc + " does not exist: " + fileOrDir.getAbsolutePath());
        if (!(fileOrDir.isDirectory() || fileOrDir.isFile()))
            throw new IllegalArgumentException(fileDesc + " must be a file or a directory: " + fileOrDir.getPath());
        if (fileOrDir.isDirectory())
        {
            if (fileOrDir.list().length == 0)
                throw new IllegalArgumentException(fileDesc + " is an empty directory: " + fileOrDir.getPath());
        }
    }

    /**
     * Returns true if the iterable contains at least one item.
     */
    public static boolean checkNotEmpty(Iterable<?> iterable)
    {
        return iterable.iterator().hasNext();
    }

}