package org.jboss.windup.config.base;

public interface Action<T> {
	public void execute(T obj);
}
