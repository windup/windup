package org.jboss.windup.engine;

import java.io.File;

import javax.inject.Inject;

import org.jboss.windup.addon.engine.WindupProcessor;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.dao.FileResourceDao;
import org.jboss.windup.graph.model.resource.FileResourceModel;
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
    private FileResourceDao fileResourceDao;

    @Inject
    private ConfigurationProcessorImpl configProcessor;

    /**
     * The entry point of the engine.
     */
    @Override
    public void execute()
    {
        // final List<GraphVisitor> visitorChain = this.provider.getSortedVisitorChain();
        //
        // LOG.info("Executing: " + visitorChain.size() + " visitors...");
        // for (final GraphVisitor visitor : visitorChain)
        // {
        // LOG.info("Processing: " + visitor + " - Class: " + visitor.getClass());
        // visitor.run();
        // }
        File r1 = new File("../../test_files/Windup1x-javaee-example.war");
        FileResourceModel r1g = fileResourceDao.createByFilePath(r1.getAbsolutePath());

        this.configProcessor.run(graphContext);

        LOG.info("Execution complete.");
    }
}
