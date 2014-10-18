package org.jboss.windup.graph.listeners;

import java.util.Map;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;

public interface AfterGraphInitializationListener
{

    void process(Map<String, Object> configuration, FramedGraph<EventGraph<TitanGraph>> graph);

}
