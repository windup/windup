package org.jboss.windup.config.condition;

public class When {
	private Condition<?> condition;
	
	public Condition<?> getCondition() {
		return condition;
	}
	
	public void setCondition(Condition<?> condition) {
		this.condition = condition;
	}
}
