package org.jboss.windup.reporting.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.FilenameUtil;

public class ReportModelService extends GraphService<ReportModel>
{
    public ReportModelService()
    {
        super(ReportModel.class);
    }

    public ReportModelService(GraphContext context)
    {
        super(context, ReportModel.class);
    }

    public void setUniqueFilename(ReportModel model, String baseFilename, String extension)
    {
        String filename = FilenameUtil.cleanFileName(baseFilename) + "." + extension;

        String outputDir = GraphService.getConfigurationModel(getGraphContext()).getOutputPath().getFilePath();
        Path outputPath = Paths.get(outputDir, filename);
        for (int i = 1; Files.exists(outputPath); i++)
        {
            filename = FilenameUtil.cleanFileName(baseFilename) + "." + i + "." + extension;
            outputPath = Paths.get(outputDir, filename);
        }

        model.setReportFilename(filename);
    }
}
