package org.jboss.windup.config.condition;

import org.jboss.windup.config.base.Action;

public class When {
	private Condition<?> condition;
	private Action<?> action;
	
	public Condition<?> getCondition() {
		return condition;
	}
	
	public void setCondition(Condition<?> condition) {
		this.condition = condition;
	}
	
	public Action<?> getAction() {
		return action;
	}
	public void setAction(Action<?> action) {
		this.action = action;
	}
}
