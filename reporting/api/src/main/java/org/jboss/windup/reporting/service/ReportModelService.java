package org.jboss.windup.reporting.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

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
        String filename = FilenameUtil.cleanFileName(baseFilename) + "." + extension;

        String outputDir = GraphService.getConfigurationModel(getGraphContext()).getOutputPath().getFilePath();
        Path outputPath = Paths.get(outputDir, filename);
        for (int i = 1; usedFilenames.contains(outputPath.toAbsolutePath().toString()); i++)
        {
            filename = FilenameUtil.cleanFileName(baseFilename) + "." + i + "." + extension;
            outputPath = Paths.get(outputDir, filename);
        }

        model.setReportFilename(filename);
    }
}
