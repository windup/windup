package org.jboss.windup.config;

import java.util.LinkedList;
import java.util.List;

import org.jboss.windup.config.base.Action;

public class Rules {
	private List<Action<?>> actions;
	
	public Rules() {
		actions = new LinkedList<Action<?>>();
	}
	
	public List<Action<?>> getActions() {
		return actions;
	}
	
	public void setActions(List<Action<?>> actions) {
		this.actions = actions;
	}
}
