package org.jboss.windup.config.actions;

import org.jboss.windup.config.base.Action;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

public class AddLinkAction<T extends Resource> implements Action<T> {

	private String description;
	private String href;
	
	@Override
	public void execute(GraphContext graphContext, T obj, LocalContext localContext) {
		
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	public String getHref() {
		return href;
	}
	
	public void setHref(String href) {
		this.href = href;
	}
	
	@Override
	public String toString() {
		return "AddLinkAction [description=" + description + ", href=" + href + "]";
	}
	
	
}
