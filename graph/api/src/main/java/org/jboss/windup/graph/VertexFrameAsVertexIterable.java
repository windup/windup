package org.jboss.windup.graph;

import java.util.Iterator;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;

public class VertexFrameAsVertexIterable implements Iterable<Vertex>
{
    private Iterable<? extends VertexFrame> frameIterable;

    public VertexFrameAsVertexIterable(Iterable<? extends VertexFrame> frameIterable)
    {
        this.frameIterable = frameIterable;
    }

    @Override
    public Iterator<Vertex> iterator()
    {
        return new Iterator<Vertex>()
        {
            private Iterator<? extends VertexFrame> frameIterator = VertexFrameAsVertexIterable.this.frameIterable
                        .iterator();

            @Override
            public void remove()
            {
                frameIterator.remove();
            }

            @Override
            public Vertex next()
            {
                return frameIterator.next().asVertex();
            }

            @Override
            public boolean hasNext()
            {
                return frameIterator.hasNext();
            }
        };
    }
}
