package org.jboss.windup.config.actions;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.jboss.windup.config.base.Action;

public class ForeachAction<T> implements Action<T> {
	
	protected final List<Action> actions;
	
	public ForeachAction() {
		this.actions = new LinkedList<Action>();
	}
	
	@Override
	public void execute(T obj) {
		throw new NotImplementedException("Not yet implemented.");
	}
	
	

	@Override
	public String toString() {
		return "ForeachAction [actions=" + actions + "]";
	}

	public List<Action> getActions() {
		return actions;
	}
	
}
