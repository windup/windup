package org.jboss.windup.config.condition;

public interface Condition<T> {
	public boolean match(T obj);
}
