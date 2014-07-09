package org.jboss.windup.config.query;

import com.tinkerpop.frames.FramedGraphQuery;

public interface QueryFramesCriterion
{
    public abstract void query(FramedGraphQuery q);
}