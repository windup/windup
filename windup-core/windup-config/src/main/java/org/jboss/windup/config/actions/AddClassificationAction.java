package org.jboss.windup.config.actions;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.config.base.Action;

public class AddClassificationAction<T> implements Action<T> {

	private String description;
	
	@Override
	public void execute(T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return "AddClassificationAction [description=" + description + "]";
	}
	
}
