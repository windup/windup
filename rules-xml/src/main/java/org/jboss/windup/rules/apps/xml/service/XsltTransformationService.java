package org.jboss.windup.rules.apps.xml.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.util.exception.WindupException;

/**
 * Contains methods for querying, creating, and deleting {@link XsltTransformationModel} objects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class XsltTransformationService extends GraphService<XsltTransformationModel>
{
    public static final String TRANSFORMEDXML_DIR_NAME = "transformedxml";

    public XsltTransformationService(GraphContext ctx)
    {
        super(ctx, XsltTransformationModel.class);
    }

    /**
     * Gets the path used for the results of XSLT Transforms.
     */
    public Path getTransformedXSLTPath()
    {
        ReportService reportService = new ReportService(getGraphContext());
        Path outputPath = Paths.get(reportService.getReportDirectory()).resolve(TRANSFORMEDXML_DIR_NAME);
        if (!Files.isDirectory(outputPath))
        {
            try
            {
                Files.createDirectories(outputPath);
            }
            catch (IOException e)
            {
                throw new WindupException("Failed to create output directory at: " + outputPath + " due to: "
                            + e.getMessage(), e);
            }
        }
        return outputPath;
    }
}
