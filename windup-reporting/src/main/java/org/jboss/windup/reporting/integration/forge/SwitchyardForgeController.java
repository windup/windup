package org.jboss.windup.reporting.integration.forge;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.metadata.type.FileMetadata;
import org.jboss.windup.metadata.type.JavaMetadata;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.type.archive.ArchiveMetadata;
import org.jboss.windup.reporting.Reporter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SwitchyardForgeController implements Reporter {
    private static final Log LOG = LogFactory.getLog(SwitchyardForgeController.class);

    private List<FileMetadata> processedEntryList = new ArrayList<FileMetadata>();

    private SwitchyardForgeRegistrar switchyardForgeRegistrar;

    @Override
    @SuppressWarnings("unchecked")
    public void process(ArchiveMetadata archive, File reportDirectory) {

        try {
            switchyardForgeRegistrar = new SwitchyardForgeRegistrar(reportDirectory);

            if (switchyardForgeRegistrar.createProject(archive.getName()) != null) {
                processJBossESB(archive);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            switchyardForgeRegistrar.stop();
            LOG.info("Furnace stopped.");
        }
    }

    /**
     * @param archive
     * @return
     */
    protected boolean processJBossESB(ArchiveMetadata archive) {

        for (FileMetadata entry : archive.getEntries()) {

            if (!processedEntryList.contains(entry)) {
                if (entry instanceof JavaMetadata) {
                    try {
                        LOG.debug("JavaFile processing: " + entry.getFilePointer().getAbsolutePath());
                        JavaFileProcessor jfp = new JavaFileProcessor(switchyardForgeRegistrar);
                        jfp.process(entry, archive.getName());
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                    processedEntryList.add(entry);

                } else if (entry instanceof XmlMetadata) {
                    try {
                        LOG.debug("XMLFile processing: " + entry.getFilePointer().getAbsolutePath());
                        XmlFileProcessor xfp = new XmlFileProcessor(switchyardForgeRegistrar);
                        xfp.process(entry, archive.getName());
                    } catch (Exception e) {
                        LOG.error(e);
                    }
                    processedEntryList.add(entry);

                }
            }
        }

        if (archive.getNestedArchives() != null) {
            for (ArchiveMetadata ar : archive.getNestedArchives()) {
                processJBossESB(ar);
            }
        }

        return false;
    }

}
