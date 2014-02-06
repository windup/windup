package org.jboss.windup.config.condition;

import java.util.LinkedList;
import java.util.List;

import org.jboss.windup.graph.GraphContext;

public class OrCondition<T> implements Condition<T> {

	private List<Condition<T>> conditions;
	
	public OrCondition() {
		conditions = new LinkedList<Condition<T>>();
	}
	
	@Override
	public boolean match(GraphContext graphContext, T obj) {
		for(Condition<T> condition : conditions) {
			if(condition.match(graphContext, obj)) {
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
