package com.tinkerpop.frames.modules.gremlingroovy;

import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.annotations.gremlin.GremlinGroovyAnnotationHandler;
import com.tinkerpop.frames.modules.AbstractModule;

/**
 * Adds <code>@GremlinGroovy</code> support to the framed graph.
 * @author Bryn Cooke
 *
 */
public class GremlinGroovyModule extends AbstractModule {
	private GremlinGroovyAnnotationHandler handler = new GremlinGroovyAnnotationHandler(); //Factory will share handler.

	@Override
	public void doConfigure(FramedGraphConfiguration config) {
		config.addMethodHandler(handler);
		
	}


}
