package org.jboss.windup.engine;

import java.util.List;
import javax.inject.Inject;
import org.jboss.windup.addon.engine.WindupProcessor;
import org.jboss.windup.engine.provider.ListenerChainProvider;
import org.jboss.windup.engine.visitor.GraphVisitor;
import org.jboss.windup.graph.GraphContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  The entry point of the engine.
 */
public class WindupProcessorImpl implements WindupProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger(WindupProcessorImpl.class);

    
    //@Inject private WindupContext windupContext;
    
    @Inject private GraphContext graphContext;

    @Inject private ListenerChainProvider provider;

    //@Inject private JavaClassDao javaClassDao;

    @Inject private ConfigurationProcessorImpl configProcessor;

    
    /**
     *  The entry point of the engine.
     */
    @Override
    public void execute()
    {
        final List<GraphVisitor> visitorChain = this.provider.getListenerChain();

        LOG.info("Executing: " + visitorChain.size() + " listeners...");
        for (final GraphVisitor visitor : visitorChain)
        {
            LOG.info("Processing: " + visitor + " - Class: " + visitor.getClass());
            visitor.run();
        }

        this.configProcessor.run(graphContext);

        LOG.info("Execution complete.");
    }
}
