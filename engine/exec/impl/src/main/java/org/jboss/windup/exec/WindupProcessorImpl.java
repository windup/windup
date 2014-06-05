package org.jboss.windup.exec;

import javax.inject.Inject;
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
    private ConfigurationProcessorImpl configProcessor;

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
