package org.jboss.windup.exec;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.config.KeepWorkDirsOption;
import org.jboss.windup.exec.configuration.WindupConfiguration;
import org.jboss.windup.util.Logging;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Util {

    private static final Logger LOG = Logging.get(Util.class);
    public static void deleteGraphDataUnlessInhibited(WindupConfiguration windupConfiguration, Path graphPath) {
        Boolean keep = (Boolean) windupConfiguration.getOptionMap().get(KeepWorkDirsOption.NAME);
        if (keep == null || !keep) {
            LOG.info("Deleting graph directory (see --" + KeepWorkDirsOption.NAME + "): " + graphPath.toFile().getPath());
            try {
                FileUtils.deleteDirectory(graphPath.toFile());
            } catch (IOException ex) {
                LOG.log(Level.WARNING, "Failed deleting graph directory: " + graphPath.toFile().getPath()
                        + System.lineSeparator() + "\tDue to: " + ex.getMessage(), ex);
            }
        }
    }

}
