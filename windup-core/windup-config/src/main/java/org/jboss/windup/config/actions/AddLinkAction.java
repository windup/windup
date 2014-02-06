package org.jboss.windup.config.actions;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.config.base.Action;

public class AddLinkAction<T> implements Action<T> {

	private String description;
	private String href;
	
	@Override
	public void execute(T obj) {
		throw new NotImplementedException("Not yet implemented.");
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
