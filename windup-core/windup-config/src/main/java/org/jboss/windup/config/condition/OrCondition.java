package org.jboss.windup.config.condition;

import java.util.LinkedList;
import java.util.List;

public class OrCondition<T> implements Condition<T> {

	private List<Condition<T>> conditions;
	
	public OrCondition() {
		conditions = new LinkedList<Condition<T>>();
	}
	
	@Override
	public boolean match(T obj) {
		for(Condition<T> condition : conditions) {
			if(condition.match(obj)) {
				return true;
			}
		}
		return false;
	}
	
	public List<Condition<T>> getConditions() {
		return conditions;
	}

	@Override
	public String toString() {
		return "OrCondition [conditions=" + conditions + "]";
	}

}
