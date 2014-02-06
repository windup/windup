package org.jboss.windup.config.actions;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.config.base.Action;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

public class AddSummaryAction<T extends Resource> implements Action<T> {

	private String description;
	
	@Override
	public void execute(GraphContext graphContext, T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "AddSummaryAction [description=" + description + "]";
	}
	
}
