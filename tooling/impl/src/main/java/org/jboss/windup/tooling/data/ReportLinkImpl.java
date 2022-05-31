package org.jboss.windup.tooling.data;

import javax.xml.bind.annotation.XmlElement;
import java.io.File;

/**
 * Correlates files in the input application with the related source report file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ReportLinkImpl implements ReportLink {
    private static final long serialVersionUID = 1L;

    private File inputFile;
    private File reportFile;

    /**
     * Contains the File path of the file in the input application.
     */
    @Override
    @XmlElement(name = "input-file")
    public File getInputFile() {
        return inputFile;
    }

    /**
     * Contains the File path of the file in the input application.
     */
    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Contains the File path of the report.
     */
    @Override
    @XmlElement(name = "report-file")
    public File getReportFile() {
        return reportFile;
    }

    /**
     * Contains the File path of the report.
     */
    public void setReportFile(File reportFile) {
        this.reportFile = reportFile;
    }
}
