package org.jboss.windup.config.condition;



public abstract class EqualCondition<T> implements Condition<T> {

	protected String value;
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

}
