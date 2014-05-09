package org.jboss.windup.addon.config.graphsearch;

import com.tinkerpop.frames.FramedGraphQuery;

public interface GraphSearchCriterion
{
    public abstract void query(FramedGraphQuery q);
}