package org.jboss.windup.configuration;

import java.io.File;

import javax.enterprise.inject.Produces;

import org.apache.commons.io.FileUtils;
import org.jboss.windup.graph.GraphContext;
import org.slf4j.LoggerFactory;

public class TestProducer {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(TestProducer.class);
	
	@Produces
	public GraphContext produceGraphContext() {
		GraphContext context = new GraphContext(new File(FileUtils.getTempDirectory(), "test-context"));
		return context;
	}
	
}
