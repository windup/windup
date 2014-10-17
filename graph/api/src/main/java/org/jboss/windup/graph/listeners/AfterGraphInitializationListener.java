package org.jboss.windup.graph.listeners;

import org.apache.commons.configuration.Configuration;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;

public interface AfterGraphInitializationListener
{

    void process(Configuration configuration, FramedGraph<EventGraph<TitanGraph>> graph);
    
}
