package org.jboss.windup.reporting.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ReportModel;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

/**
 * Convenient search and creation methods for {@link ReportModel}.
 */
public class ReportService extends GraphService<ReportModel>
{
    private static final String REPORTS_DIR = "reports";
    private static final String DATA = "data";

    private static final Set<String> usedFilenames = new HashSet<>();


    /**
     * Used to insure uniqueness in report names
     */
    private final AtomicInteger index = new AtomicInteger(1);

    public ReportService(GraphContext context)
    {
        super(context, ReportModel.class);
    }

    public Path getReportDataDirectory()
    {
        Path path = getReportDirectory().resolve(DATA);
        createDirectoryIfNeeded(path);
        return path;
    }

    /**
     * Returns the output directory for reporting.
     */
    public Path getReportDirectory()
    {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(getGraphContext());
        Path path = cfg.getOutputPath().asFile().toPath().resolve(REPORTS_DIR);
        createDirectoryIfNeeded(path);
        return path.toAbsolutePath();
    }

    private void createDirectoryIfNeeded(Path path)
    {
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
    }

    /**
     * Returns the ReportModel with given name.
     */
    @SuppressWarnings("unchecked")
    public <T extends ReportModel> T getReportByName(String name, Class<T> clazz)
    {
        WindupVertexFrame model = this.getUniqueByProperty(ReportModel.REPORT_NAME, name);
        try
        {
            return (T) model;
        }
        catch (ClassCastException ex)
        {
            throw new WindupException("The vertex is not of expected frame type " + clazz.getName() + ": " + model.toPrettyString());
        }
    }

    /**
     * Gets a unique filename (that has not been used before in the output folder) for this report and sets it on the report model.
     */
    public void setUniqueFilename(ReportModel model, String baseFilename, String extension)
    {
        String filename = PathUtil.cleanFileName(baseFilename) + "." + extension;

        // FIXME this looks nasty
        while (usedFilenames.contains(filename))
        {
            filename = PathUtil.cleanFileName(baseFilename) + "." + index.getAndIncrement() + "." + extension;
        }
        usedFilenames.add(filename);

        model.setReportFilename(filename);
    }
}
