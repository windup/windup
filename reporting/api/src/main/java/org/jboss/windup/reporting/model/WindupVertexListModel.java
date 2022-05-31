package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Iterator;
import java.util.List;

/**
 * Contains a list of {@link WindupVertexFrame} objects and (for convenience) implements the {@link Iterable} interface as well.
 * <p>
 * NOTE that this currently doesn't keep the order of the elements. It is an unordered collection.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(WindupVertexListModel.TYPE)
public interface WindupVertexListModel<T extends WindupVertexFrame> extends WindupVertexFrame, Iterable<T> {
    String TYPE = "WindupVertexListModel";

    /**
     * Returns the list as an {@link Iterable<T>}.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    List<T> getList();

    /**
     * Sets the items in the list.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    void setList(List<T> list);

    /**
     * Adds the provided item to the list.
     */
    @Adjacency(label = "list", direction = Direction.OUT)
    void addItem(T item);

    /**
     * Adds all of the items to the list.
     */
    @JavaHandler(handler = Impl.class)
    WindupVertexListModel<T> addAll(Iterable<T> items);

    /**
     * Returns an {@link Iterator} for this list.
     */
    @Override
    @JavaHandler(handler = Impl.class)
    Iterator<T> iterator();

    class Impl<T extends WindupVertexFrame> {
        public WindupVertexListModel<T> addAll(WindupVertexListModel<T> frame, Iterable<T> items) {
            for (T item : items)
                frame.addItem(item);

            return frame;
        }

        public Iterator<T> iterator(WindupVertexListModel<T> frame) {
            return frame.getList().iterator();
        }
    }
}
