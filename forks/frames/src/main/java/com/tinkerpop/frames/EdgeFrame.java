package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Edge;

/**
 * An interface for Edge-based frames which provides access to the underlying Edge.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface EdgeFrame {
    Edge asEdge();
}
