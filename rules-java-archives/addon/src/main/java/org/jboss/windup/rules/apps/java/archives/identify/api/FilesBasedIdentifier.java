package org.jboss.windup.rules.apps.java.archives.identify.api;

import java.io.File;


/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface FilesBasedIdentifier extends ArchiveHashIdentifier
{
    public void addMappingsFrom(File file);
}
