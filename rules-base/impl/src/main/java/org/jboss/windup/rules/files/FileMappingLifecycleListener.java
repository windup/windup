package org.jboss.windup.rules.files;

import java.util.logging.Logger;

import org.apache.tinkerpop.gremlin.process.traversal.TraversalStrategies;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.EventStrategy;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.janusgraph.graphdb.database.StandardJanusGraph;
import org.jboss.windup.config.AbstractRuleLifecycleListener;
import org.jboss.windup.config.GraphRewrite;

public class FileMappingLifecycleListener extends AbstractRuleLifecycleListener
{
    private static final Logger LOG = Logger.getLogger(FileMappingLifecycleListener.class.getName());

    @Override
    public void beforeExecution(GraphRewrite event)
    {
        LOG.info("Registered " + FileMappingGraphChangedListener.class.getSimpleName()
                    + "- Mapped file types will be added to the graph automatically.");

        TraversalStrategies graphStrategies = TraversalStrategies.GlobalCache
                .getStrategies(Graph.class)
                .clone()
                .addStrategies(EventStrategy.build().addListener(new FileMappingGraphChangedListener(event)).create());
        //TraversalStrategies.GlobalCache.registerStrategies(StandardJanusGraph.class, graphStrategies);
    }
}
