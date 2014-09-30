package com.tinkerpop.frames;

import com.tinkerpop.blueprints.Vertex;

/**
 * An interface for Vertex-based frames which provides access to the underlying Vertex.
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public interface VertexFrame {
    Vertex asVertex();
}
