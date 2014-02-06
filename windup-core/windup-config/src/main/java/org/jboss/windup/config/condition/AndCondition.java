package org.jboss.windup.config.condition;

import java.util.LinkedList;
import java.util.List;

public class AndCondition<T> implements Condition<T> {

	private List<Condition<T>> conditions;
	
	public AndCondition() {
		this.conditions = new LinkedList<Condition<T>>();
	}
	
	@Override
	public boolean match(T obj) {
		for(Condition<T> condition : conditions) {
			if(!condition.match(obj)) {
				return false;
			}
		}
		return true;
	}
	
	public List<Condition<T>> getConditions() {
		return conditions;
	}

	@Override
	public String toString() {
		return "AndCondition [conditions=" + conditions + "]";
	}

}
