package org.jboss.windup.config.base;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

public interface Action<T extends Resource> {
	public void execute(GraphContext graphContext, T obj);
}
