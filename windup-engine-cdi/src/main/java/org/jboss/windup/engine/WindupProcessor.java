package org.jboss.windup.engine;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.engine.qualifier.ListenerChainQualifier;
import org.jboss.windup.engine.visitor.base.GraphVisitor;
import org.jboss.windup.graph.dao.JavaClassDao;
import org.jboss.windup.graph.model.resource.JavaClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupProcessor {

	private static final Logger LOG = LoggerFactory.getLogger(WindupProcessor.class);
	
	@Inject
	WindupContext windupContext;
	
	@ListenerChainQualifier
	@Inject 
	List<GraphVisitor> listenerChain;
	
	@Inject
	JavaClassDao javaClassDao;
	
	public void execute() {
		LOG.info("Executing: "+listenerChain.size() +" listeners...");
		for(GraphVisitor visitor : listenerChain) {
			LOG.info("Processing: "+visitor.getClass());
			visitor.visitContext(windupContext);
		}
		LOG.info("Execution complete.");
	}
}
