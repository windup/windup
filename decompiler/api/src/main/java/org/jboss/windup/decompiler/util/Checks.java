package org.jboss.windup.decompiler.util;

import java.io.File;


/**
 *
 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class Checks {

    // --- Input validating methods. ---

    public static void checkFileToBeRead(File archive, String fileDesc) throws IllegalArgumentException
    {
        if (archive == null)
            throw new IllegalArgumentException(fileDesc + " must not be null.");
        if (!archive.exists())
            throw new IllegalArgumentException(fileDesc + " does not exist: " + archive.getAbsolutePath());
        if (archive.isDirectory())
            throw new IllegalArgumentException(fileDesc + " is a directory, expected a file: "
                    + archive.getPath());
    }


    public static  void checkDirectoryToBeFilled(File outputDir, String dirDesc) throws IllegalArgumentException
    {
        if (outputDir == null)
            throw new IllegalArgumentException(dirDesc + " must not be null.");
        if (outputDir.exists() && !outputDir.isDirectory())
            throw new IllegalArgumentException(dirDesc + " is a file, expected a directory: "
                    + outputDir.getAbsolutePath());
    }


    public static  void checkDirectoryToBeRead(File rootDir, String dirDesc)
    {
        if (rootDir == null)
            throw new IllegalArgumentException(" must not be null.");
        if (!rootDir.exists())
            throw new IllegalArgumentException(" does not exist: " + rootDir.getPath());
        if (!rootDir.isDirectory())
            throw new IllegalArgumentException(" is a file, expected a directory: " + rootDir.getPath());
    }    

}// class
