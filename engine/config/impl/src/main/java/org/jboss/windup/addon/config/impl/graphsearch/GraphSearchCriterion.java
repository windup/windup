package org.jboss.windup.addon.config.impl.graphsearch;

import com.tinkerpop.frames.FramedGraphQuery;

public abstract class GraphSearchCriterion
{
    public abstract void query(FramedGraphQuery q);
}