package org.jboss.windup.config.condition;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.graph.GraphContext;


public class MD5Condition<T> extends EqualCondition<T> {

	@Override
	public boolean match(GraphContext graphContext, T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}

	@Override
	public String toString() {
		return "MD5Condition [value=" + value + "]";
	}
}
