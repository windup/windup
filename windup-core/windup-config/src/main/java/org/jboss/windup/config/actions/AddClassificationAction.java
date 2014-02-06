package org.jboss.windup.config.actions;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.config.base.Action;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

public class AddClassificationAction<T extends Resource> implements Action<T> {

	private String description;
	
	@Override
	public void execute(GraphContext graphContext, T obj, LocalContext localContext) {
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
