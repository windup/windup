package org.jboss.windup.reporting.renderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.util.exception.WindupException;

public abstract class AbstractGraphRenderer implements GraphRenderer
{

    @Inject
    private GraphContext graphContext;

    protected Path createOutputFolder(String name)
    {
        Path outputFolder = getOutputPath().resolve(name);
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

    private Path getOutputPath()
    {
        return Paths.get(getConfiguration().getOutputPath().getFilePath(), "renderedGraph");
    }

    protected WindupConfigurationModel getConfiguration()
    {
        return GraphService.getConfigurationModel(graphContext);
    }

    protected GraphContext getContext()
    {
        return graphContext;
    }
}
