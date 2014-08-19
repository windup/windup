package org.jboss.windup.reporting.service;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.FilenameUtil;

/**
 * Convenient search and creation methods for ReportModel.
 * 
 */
public class ReportModelService extends GraphService<ReportModel>
{
    private static Set<String> usedFilenames = new HashSet<>();

    /**
     * Used to insure uniqueness in report names
     */
    private AtomicInteger index = new AtomicInteger(1);

    public ReportModelService()
    {
        super(ReportModel.class);
    }

    public ReportModelService(GraphContext context)
    {
        super(context, ReportModel.class);
    }

    /**
     * Gets a unique filename (that has not been used before in the output folder) for this report and sets it on the
     * report model.
     */
    public void setUniqueFilename(ReportModel model, String baseFilename, String extension)
    {
        String filename = FilenameUtil.cleanFileName(baseFilename) + "." + index.getAndIncrement() + "." + extension;

        for (int i = 1; usedFilenames.contains(filename.toString()); i++)
        {
            filename = FilenameUtil.cleanFileName(baseFilename) + "." + index.getAndIncrement() + "." + extension;
        }

        model.setReportFilename(filename);
    }
}
