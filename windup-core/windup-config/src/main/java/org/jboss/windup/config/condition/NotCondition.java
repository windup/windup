package org.jboss.windup.config.condition;

import org.jboss.windup.graph.GraphContext;


public class NotCondition<T> implements Condition<T> {

	private Condition<T> condition;
	
	@Override
	public boolean match(GraphContext graphContext, T obj) {
		//return negation
		return !(condition.match(graphContext, obj));
	}
	
	public Condition<T> getCondition() {
		return condition;
	}
	
	public void setCondition(Condition<T> condition) {
		this.condition = condition;
	}

	@Override
	public String toString() {
		return "NotCondition [conditions=" + condition + "]";
	}

}
