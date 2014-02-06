package org.jboss.windup.config.base;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.windup.config.condition.Condition;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.Resource;

@XmlRootElement
public class BaseAction<T extends Resource> implements Action<T> {

	protected Condition<T> condition;
	
	protected final List<Action<T>> actions;

	public BaseAction() {
		actions = new LinkedList<Action<T>>();
	}
	
	@Override
	public void execute(GraphContext graphContext, T obj) {
		if(condition != null && !condition.match(graphContext, obj)) {
			//return without executing.
			return;
		}

		for(Action<T> action : actions) {
			action.execute(null, obj);
		}
	}

	public Condition<T> getCondition() {
		return condition;
	}

	public void setCondition(Condition<T> condition) {
		this.condition = condition;
	}

	public List<Action<T>> getActions() {
		return actions;
	}

	@Override
	public String toString() {
		return "BaseAction [condition=" + condition + ", actions=" + actions + "]";
	}
	
	
}
