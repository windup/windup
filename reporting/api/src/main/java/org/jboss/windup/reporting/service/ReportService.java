package org.jboss.windup.reporting.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.WindupPathUtil;
import org.jboss.windup.util.exception.WindupException;

/**
 * Convenient search and creation methods for {@link ReportModel}.
 *
 */
public class ReportService extends GraphService<ReportModel>
{
    private static final String REPORTS_DIR = "reports";

    private static Set<String> usedFilenames = new HashSet<>();

    /**
     * Used to insure uniqueness in report names
     */
    private AtomicInteger index = new AtomicInteger(1);

    public ReportService(GraphContext context)
    {
        super(context, ReportModel.class);
    }

    /**
     * Returns the output directory for reporting.
     */
    public String getReportDirectory()
    {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(getGraphContext());
        Path path = cfg.getOutputPath().asFile().toPath().resolve(REPORTS_DIR);
        if (!Files.isDirectory(path))
        {
            try
            {
                Files.createDirectories(path);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create directory: " + path.toString() + " due to: "
                            + e.getMessage(), e);
            }
        }
        return path.toAbsolutePath().toString();
    }

    /**
     * Gets a unique filename (that has not been used before in the output folder) for this report and sets it on the report model.
     */
    public void setUniqueFilename(ReportModel model, String baseFilename, String extension)
    {
        String filename = WindupPathUtil.cleanFileName(baseFilename) + "." + extension;

        // FIXME this looks nasty
        while (usedFilenames.contains(filename.toString()))
        {
            filename = WindupPathUtil.cleanFileName(baseFilename) + "." + index.getAndIncrement() + "." + extension;
        }
        usedFilenames.add(filename);

        model.setReportFilename(filename);
    }
}
