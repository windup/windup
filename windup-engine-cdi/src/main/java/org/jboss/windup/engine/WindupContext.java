package org.jboss.windup.engine;

import java.io.File;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.GraphContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named("windup-context")
@ApplicationScoped
public class WindupContext {

	private static final Logger LOG = LoggerFactory.getLogger(WindupContext.class);
	
	private final File runDirectory;
	private final GraphContext graphContext;
	
	public WindupContext() {
		runDirectory = new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString());
		graphContext = new GraphContext(new File(runDirectory, "windup-graph"));
	}
	
	public GraphContext getGraphContext() {
		return graphContext;
	}
	
	public File getRunDirectory() {
		return runDirectory;
	}
}
