package org.jboss.windup.config.condition;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.graph.GraphContext;


public class DTDPublicIdCondition<T> extends MatchesCondition<T> {

	@Override
	public boolean match(GraphContext graphContext, T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}

	@Override
	public String toString() {
		return "DTDPublicIdCondition [pattern=" + pattern + "]";
	}
}
