package org.jboss.windup.reporting.renderer;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author jsigtler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 * @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public abstract class AbstractGraphRenderer implements GraphRenderer {
    protected Path createOutputFolder(GraphContext graphContext, String name) {
        Path outputPath = getOutputPath(graphContext);
        Path outputFolder = outputPath.resolve(name);
        try {
            Files.createDirectories(outputFolder);
        } catch (IOException e) {
            throw new WindupException("Failed to write graph due to: " + e.getMessage(), e);
        }
        return outputFolder;
    }

    private Path getOutputPath(GraphContext graphContext) {
        return new ReportService(graphContext).getReportDirectory().resolve("renderedGraph");
    }
}
