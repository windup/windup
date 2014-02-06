package org.jboss.windup.config.condition;

import org.apache.commons.lang.NotImplementedException;


public class SHA1Condition<T> extends EqualCondition<T> {

	@Override
	public boolean match(T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}

	@Override
	public String toString() {
		return "SHA1Condition [value=" + value + "]";
	}
}
