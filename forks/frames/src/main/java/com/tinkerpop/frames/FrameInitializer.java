package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Element;

/**
 * Allows new framed vertices and edges to be initialized before they are returned to the user. This can be used for defaulting of properties.
 * Instances of this class should be threadsafe.
 * 
 * @author Bryn Cooke
 */
public interface FrameInitializer {
    /**
     * @param kind        The kind of frame.
     * @param framedGraph The graph.
     * @param element     The new element that is being inserted into the graph.
     */
    public void initElement(final Class<?> kind, final FramedGraph<?> framedGraph, final Element element);
}
