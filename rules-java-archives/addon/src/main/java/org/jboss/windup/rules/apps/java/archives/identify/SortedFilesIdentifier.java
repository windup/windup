package org.jboss.windup.rules.apps.java.archives.identify;


import java.io.File;
import javax.inject.Singleton;
import org.jboss.windup.rules.apps.java.archives.identify.api.FilesBasedIdentifier;

/**
 * SortedFileIdentifier can only handle a single file.
 * To be able to use binary search from multiple files, this class is used.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@Singleton
public class SortedFilesIdentifier extends CompositeIdentifier implements FilesBasedIdentifier
{
    private static final SortedFilesIdentifier instance = new SortedFilesIdentifier();

    public static SortedFilesIdentifier getInstance()
    {
        return instance;
    }

    @Override
    public void addMappingsFrom(File file)
    {
        this.addIdentifier(new SortedFileArchiveIdentifier(file));
    }

}
