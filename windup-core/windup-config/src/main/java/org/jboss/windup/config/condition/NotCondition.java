package org.jboss.windup.config.condition;


public class NotCondition<T> implements Condition<T> {

	private Condition<T> condition;
	
	@Override
	public boolean match(T obj) {
		//return negation
		return !(condition.match(obj));
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
