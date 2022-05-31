package org.jboss.windup.rules.apps.xml.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.rules.apps.xml.model.XsltTransformationModel;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains methods for querying, creating, and deleting {@link XsltTransformationModel} objects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class XsltTransformationService extends GraphService<XsltTransformationModel> {
    public static final String TRANSFORMEDXML_DIR_NAME = "transformedxml";

    public XsltTransformationService(GraphContext ctx) {
        super(ctx, XsltTransformationModel.class);
    }

    /**
     * Gets the path used for the results of XSLT Transforms.
     */
    public Path getTransformedXSLTPath(FileModel payload) {
        ReportService reportService = new ReportService(getGraphContext());
        Path outputPath = reportService.getReportDirectory();
        outputPath = outputPath.resolve(this.getRelativeTransformedXSLTPath(payload));
        if (!Files.isDirectory(outputPath)) {
            try {
                Files.createDirectories(outputPath);
            } catch (IOException e) {
                throw new WindupException("Failed to create output directory at: " + outputPath + " due to: "
                        + e.getMessage(), e);
            }
        }
        return outputPath;
    }

    public Path getRelativeTransformedXSLTPath(FileModel payload) {
        Path outputPath = Paths.get("");
        if (payload != null) {
            String ancestorFolder = payload.getProjectModel().getRootProjectModel().getName();
            outputPath = outputPath.resolve(PathUtil.cleanFileName(ancestorFolder));
            if (!ancestorFolder.equals(payload.getProjectModel().getName())) {
                outputPath = outputPath.resolve(PathUtil.cleanFileName(payload.getProjectModel().getName()));
            }
        }
        outputPath = outputPath.resolve(TRANSFORMEDXML_DIR_NAME);
        return outputPath;
    }

}
