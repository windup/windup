package org.jboss.windup.exec;

import java.nio.file.Path;

import javax.inject.Inject;

import org.jboss.forge.furnace.util.Predicate;
import org.jboss.windup.config.ConfigurationProcessor;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The entry point of the engine.
 */
public class WindupProcessorImpl implements WindupProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger(WindupProcessorImpl.class);

    @Inject
    private GraphContext graphContext;

    @Inject
    private ConfigurationProcessor configProcessor;

    private Path outputDirectory;

    @Override
    public void setOutputDirectory(Path outputDirectory)
    {
        this.outputDirectory = outputDirectory;

        Path graphDirectory = outputDirectory.resolve("graph");
        graphContext.setGraphDirectory(graphDirectory);
    }

    @Override
    public void execute(Predicate<WindupRuleProvider> ruleProviderFilter)
    {
        this.configProcessor.run(graphContext, ruleProviderFilter);
    }

    /**
     * The entry point of the engine.
     */
    @Override
    public void execute()
    {
        this.configProcessor.run(graphContext);

        LOG.info("Execution complete.");
    }
}
