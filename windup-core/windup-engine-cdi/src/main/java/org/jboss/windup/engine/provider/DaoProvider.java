package org.jboss.windup.engine.provider;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.windup.engine.WindupContext;
import org.jboss.windup.graph.GraphContext;


public class DaoProvider {
	
	@Inject
	private WindupContext context;
	
	@Produces
	public GraphContext produceGraphContext() {
		return context.getGraphContext();
	}
	
	
}