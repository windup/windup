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
 * NOTE that this currently doesn't keep the order of the elements. So it's more of a 
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(WindupVertexListModel.TYPE)
public interface WindupVertexListModel<T extends WindupVertexFrame> extends WindupVertexFrame, Iterable<T>
{
    String TYPE = "WindupVertexListModel";

    /**
     * Returns the list as an {@link Iterable<T>}.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    Iterable<T> getList();

    /**
     * Sets the items in the list.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    WindupVertexListModel<T> setList(Iterable<T> list);

    /**
     * Adds the provided item to the list.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    WindupVertexListModel<T> addItem(T item);

    /**
     * Adds all of the items to the list.
     */
    @JavaHandler
    WindupVertexListModel<T> addAll(Iterable<T> items);

    /**
     * Returns an {@link Iterator} for this list.
     */
    @Override
    @JavaHandler
    Iterator<T> iterator();

    abstract class Impl<T extends WindupVertexFrame> implements WindupVertexListModel<T>, JavaHandlerContext<Vertex>
    {
        @Override
        public WindupVertexListModel<T> addAll(Iterable<T> items)
        {
            for (T item : items)
                addItem(item);

            return this;
        }

        @Override
        public Iterator<T> iterator()
        {
            return getList().iterator();
        }
    }
}
