package org.jboss.windup.config.condition;

import org.apache.commons.lang.NotImplementedException;


public class FilenameCondition<T> extends MatchesCondition<T> {

	@Override
	public boolean match(T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}

	@Override
	public String toString() {
		return "FilenameCondition [pattern=" + pattern + "]";
	}
}
