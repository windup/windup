package org.jboss.windup.decompiler.fernflower;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.jar.Manifest;

import org.jboss.windup.decompiler.api.DecompilationListener;
import org.jboss.windup.util.exception.WindupStopException;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

/**
 * This handles saving the resutls after fernflower completes the decompilation of one (or more) .class files.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FernFlowerResultSaver implements IResultSaver
{
    private final List<String> sourceClassFiles;
    private final File outputDirectory;
    private final DecompilationListener listener;

    private boolean fileSaved = false;

    /**
     * Creates a {@link IResultSaver} for this single classfile. Each instance should not be reused.
     */
    public FernFlowerResultSaver(List<String> sourceClassFiles, File outputDir, DecompilationListener listener)
    {
        this.sourceClassFiles = sourceClassFiles;
        this.outputDirectory = outputDir;
        this.listener = listener;
    }


    /**
     * Indicates that this file
     */
    public boolean isFileSaved()
    {
        return fileSaved;
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping)
    {
        File outputFile = new File(outputDirectory, qualifiedName + ".java");
        try
        {
            if (!outputFile.getParentFile().isDirectory())
                outputFile.getParentFile().mkdirs();

            try (FileWriter fw = new FileWriter(outputFile))
            {
                fw.write(content);
            }
            if (listener != null) {
                if (mapping != null) {
                    listener.fileDecompiled(sourceClassFiles, outputFile.toString(), mapping);
                } else {
                    listener.fileDecompiled(sourceClassFiles, outputFile.toString());
                }
            }

            fileSaved = true;

        }
        catch (IOException t)
        {
            if (listener != null)
                listener.decompilationFailed(sourceClassFiles, t.getMessage());
        }
        catch (WindupStopException ex)
        {
            // Rethrowing to explicitly show where WindupStopException bubbles through.
            throw ex;
        }
    }

    @Override
    public void saveFolder(String path)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void copyFile(String source, String path, String entryName)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entry)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }

    @Override
    public void closeArchive(String path, String archiveName)
    {
        // Not implemented as it is not needed for our Fernflower usage.
    }
}
