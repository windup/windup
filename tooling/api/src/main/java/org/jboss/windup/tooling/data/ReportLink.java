package org.jboss.windup.tooling.data;

import java.io.File;
import java.io.Serializable;

/**
 * Correlates files in the input application with the related source report file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface ReportLink extends Serializable {
    /**
     * Contains the File path of the file in the input application.
     */
    public File getInputFile();

    /**
     * Contains the File path of the report.
     */
    public File getReportFile();
}
