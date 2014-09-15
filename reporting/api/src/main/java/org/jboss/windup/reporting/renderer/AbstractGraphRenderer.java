package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.util.exception.WindupException;

/**
 * @author jsigtler
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public abstract class AbstractGraphRenderer implements GraphRenderer
{
    protected Path createOutputFolder(WindupConfigurationModel configuration, String name)
    {
        Path outputPath = getOutputPath(configuration);
        Path outputFolder = outputPath.resolve(name);
        try
        {
            Files.createDirectories(outputFolder);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to write graph due to: " + e.getMessage(), e);
        }
        return outputFolder;
    }

    private Path getOutputPath(WindupConfigurationModel configuration)
    {
        return Paths.get(configuration.getOutputPath().getFilePath(), "renderedGraph");
    }
}
