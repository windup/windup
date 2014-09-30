package com.tinkerpop.frames;

import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Predicate;
import com.tinkerpop.blueprints.Query.Compare;

/**
 * GraphQuery that allows framing of results. 
 * 
 * @author Bryn Cooke
 *
 */
public interface FramedGraphQuery extends GraphQuery {
	@Override
    public FramedGraphQuery has(String key);

    @Override
    public FramedGraphQuery hasNot(String key);

    @Override
    public FramedGraphQuery has(String key, Object value);

    @Override
    public FramedGraphQuery hasNot(String key, Object value);

    @Override
    public FramedGraphQuery has(String key, Predicate predicate, Object value);

    @Override
    @Deprecated
    public <T extends Comparable<T>> FramedGraphQuery has(String key, T value, Compare compare);

    @Override
    public <T extends Comparable<?>> FramedGraphQuery interval(String key, T startValue, T endValue);

    @Override
    public FramedGraphQuery limit(int limit);
    

    /**
     * Execute the query and return the matching edges.
     *
     * @param the default annotated interface to frame the edge as
     * @return the unfiltered incident edges
     */
    public <T> Iterable<T> edges(Class<T> kind);

    /**
     * Execute the query and return the vertices on the other end of the matching edges.
     *
     * @param the default annotated interface to frame the vertex as
     * @return the unfiltered adjacent vertices
     */
	public <T> Iterable<T> vertices(Class<T> kind);
}
