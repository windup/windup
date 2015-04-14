package org.jboss.windup.reporting.model;

import java.util.Iterator;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Contains a list of {@link WindupVertexFrame} objects and (for convenience) implements the {@link Iterable} interface as well.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
@TypeValue(WindupVertexListModel.TYPE)
public interface WindupVertexListModel<T extends WindupVertexFrame> extends WindupVertexFrame, Iterable<T>
{
    public static final String TYPE = "WindupVertexListModel";

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<T> getList();

    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<T> setList(Iterable<T> list);

    @Adjacency(label = "list", direction = Direction.OUT)
    void addItem(T item);

    @Override
    @JavaHandler
    Iterator<T> iterator();

    abstract class Impl<T extends WindupVertexFrame> implements WindupVertexListModel<T>, JavaHandlerContext<Vertex>
    {
        @Override
        public Iterator<T> iterator()
        {
            return getList().iterator();
        }
    }
}
