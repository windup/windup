package org.jboss.windup.rules.apps.java.scan.operation;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.util.Logging;

import java.io.File;
import java.io.FileFilter;
import java.util.logging.Logger;


/**
 * Accepts certain known files from an archive, like MANIFEST.MF, Maven related, and licenses.
 */
public class IdentifiedArchiveFileFilter implements FileFilter {
    private static final Logger LOG = Logging.get(IdentifiedArchiveFileFilter.class);

    private final String archiveName;

    public IdentifiedArchiveFileFilter(ArchiveModel archive) {
        this.archiveName = archive.getArchiveName();
    }

    @Override
    public boolean accept(File file) {
        if (file.isFile()) {
            // only accept MANIFEST, POM.xml, and License files...
            if (StringUtils.equals(file.getName(), "MANIFEST.MF")
                    || StringUtils.equals(file.getName(), "pom.xml")
                    || StringUtils.equals(file.getName(), "pom.properties")
                    || StringUtils.containsIgnoreCase(file.getName(), "license")
                    || StringUtils.containsIgnoreCase(file.getName(), "notice")
                    || StringUtils.containsIgnoreCase(file.getName(), "lgpl")
                    || StringUtils.containsIgnoreCase(file.getName(), "gpl")) {
                LOG.info(archiveName + " - Accepting File on Identified Archive: " + file.getName() + ".");
                return true;
            }

            if (LOG.getLevel() == java.util.logging.Level.FINE) {
                LOG.fine(archiveName + " - Rejecting File on Identified Archive: " + file.getName() + ".");
            }
            return false;
        }

        return true;
    }

}
