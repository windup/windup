package org.jboss.windup.config.condition;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.graph.GraphContext;


public class FilenameCondition<T> extends MatchesCondition<T> {

	@Override
	public boolean match(GraphContext graphContext, T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}

	@Override
	public String toString() {
		return "FilenameCondition [pattern=" + pattern + "]";
	}
}
