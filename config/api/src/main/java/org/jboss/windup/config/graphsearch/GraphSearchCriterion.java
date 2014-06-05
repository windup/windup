package org.jboss.windup.config.graphsearch;

import com.tinkerpop.frames.FramedGraphQuery;

public interface GraphSearchCriterion
{
    public abstract void query(FramedGraphQuery q);
}