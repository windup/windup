package com.tinkerpop.frames.modules;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.FramedGraphConfiguration;
import com.tinkerpop.frames.FramedGraphFactory;
import com.tinkerpop.frames.annotations.AnnotationHandler;

/**
 * A module is a group of functionality that must be configured on a
 * FramedGraph. They are used by {@link FramedGraphFactory} to create a
 * configuration for each graphs produced by that factory.
 * 
 * Modules may add {@link FrameInitializer}s {@link TypeResolver}s and
 * {@link AnnotationHandler}s to the configuration.
 * 
 * Modules may wrap the graph being framed. For example to add an event graph.
 * 
 * Modules should be fast and light weight as configure will be called for each {@link FramedGraph} created. 
 * 
 * @see FramedGraphFactory
 * 
 * @author Bryn Cooke
 */
public interface Module {
	/**
	 * @param baseGraph The graph being framed.
	 * @param config The configuration for the new FramedGraph.
	 * @return The graph being framed.
	 */
	Graph configure(Graph baseGraph, FramedGraphConfiguration config);
	
	

}
