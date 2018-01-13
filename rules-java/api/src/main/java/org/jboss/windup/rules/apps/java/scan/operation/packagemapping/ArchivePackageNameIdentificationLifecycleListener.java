package org.jboss.windup.rules.apps.java.scan.operation.packagemapping;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;

import java.util.logging.Logger;

/**
 * Registers the {@link ArchivePackageNameIdentificationGraphChangedListener}.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jess Sightler</a>
 */
public class ArchivePackageNameIdentificationLifecycleListener extends AbstractRuleLifecycleListener
{
    private static final Logger LOG = Logger.getLogger(ArchivePackageNameIdentificationLifecycleListener.class.getName());

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        LOG.info("Registered " + ArchivePackageNameIdentificationGraphChangedListener.class.getSimpleName() + " - vendors will automatically be identified.");

        TraversalStrategies graphStrategies = TraversalStrategies.GlobalCache
                .getStrategies(Graph.class)
                .clone()
                .addStrategies(EventStrategy.build().addListener(new ArchivePackageNameIdentificationGraphChangedListener(event)).create());
        //TraversalStrategies.GlobalCache.registerStrategies(StandardJanusGraph.class, graphStrategies);
    }
}
