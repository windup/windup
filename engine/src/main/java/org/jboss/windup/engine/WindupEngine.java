package org.jboss.windup.engine;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindupEngine {
	private static final Logger LOG = LoggerFactory.getLogger(WindupEngine.class);
	
    public static void main(String[] args) throws FileNotFoundException, IOException {
    	Weld weld = new Weld();
    	WeldContainer container = weld.initialize();
    	
    	WindupProcessor processor = container.instance().select(WindupProcessor.class).get();
    	processor.execute();
    	
    	weld.shutdown();
    }
}