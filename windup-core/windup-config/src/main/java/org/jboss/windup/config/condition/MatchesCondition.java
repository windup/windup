package org.jboss.windup.config.condition;

import java.util.regex.Pattern;

public abstract class MatchesCondition<T> implements Condition<T> {

	protected Pattern pattern;
	
	public void setPattern(Pattern pattern) {
		this.pattern = pattern;
	}

}
