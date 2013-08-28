package org.jboss.windup.reporting.integration.forge;

import org.jboss.windup.metadata.type.FileMetadata;

/**
 * User: rsearls
 * Date: 8/28/13
 */
public interface FileProcessor {
    public void process(FileMetadata entry, String archiveName);
}
