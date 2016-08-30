package org.jboss.windup.tooling.data;

import java.io.File;

import org.jboss.windup.tooling.data.ReportLink;

/**
 * Correlates files in the input application with the related source report file.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ReportLinkImpl implements ReportLink
{
    private File inputFile;
    private File reportFile;

    /**
     * Contains the File path of the file in the input application.
     */
    @Override
    public File getInputFile()
    {
        return inputFile;
    }

    /**
     * Contains the File path of the file in the input application.
     */
    public void setInputFile(File inputFile)
    {
        this.inputFile = inputFile;
    }

    /**
     * Contains the File path of the report.
     */
    @Override
    public File getReportFile()
    {
        return reportFile;
    }

    /**
     * Contains the File path of the report.
     */
    public void setReportFile(File reportFile)
    {
        this.reportFile = reportFile;
    }
}
