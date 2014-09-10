package org.jboss.windup.decompiler.procyon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.strobel.decompiler.DecompilerSettings;

/**
 * This just adds UTF-8 if isUnicideOutputEnabled. To be dumped?
 */
final class FileOutputWriter extends OutputStreamWriter
{
    private final File file;

    FileOutputWriter(final File file, final DecompilerSettings settings) throws IOException
    {
        super(new FileOutputStream(file), Charset.forName("UTF-8"));
        this.file = file;
    }

    /**
     * @returns the file to which 'this' is writing.
     */
    public File getFile()
    {
        return this.file;
    }
}