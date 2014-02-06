package org.jboss.windup.config.condition;

import org.jboss.windup.graph.GraphContext;

public interface Condition<T> {
	public boolean match(GraphContext graphContext, T obj);
}
