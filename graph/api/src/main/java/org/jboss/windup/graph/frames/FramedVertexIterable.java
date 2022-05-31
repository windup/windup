package org.jboss.windup.graph.frames;

import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.VertexFrame;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Iterator;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FramedVertexIterable<T extends VertexFrame> implements Iterable<T> {
    private final FramedGraph framedGraph;
    private final Iterable<Vertex> vertices;
    private final Class<T> type;

    public FramedVertexIterable() {
        this.framedGraph = null;
        this.vertices = null;
        this.type = null;
    }

    public FramedVertexIterable(FramedGraph framedGraph, Iterable<Vertex> vertices, Class<T> type) {
        this.framedGraph = framedGraph;
        this.vertices = vertices;
        this.type = type;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private Iterator<Vertex> iterator = vertices.iterator();

            public void remove() {
                throw new UnsupportedOperationException();
            }

            public boolean hasNext() {
                return this.iterator.hasNext();
            }

            public T next() {
                return framedGraph.frameElement(this.iterator.next(), type);
            }
        };
    }
}
