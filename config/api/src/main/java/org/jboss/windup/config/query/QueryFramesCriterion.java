package org.jboss.windup.config.query;

import com.syncleus.ferma.Traversable;

public interface QueryFramesCriterion {
    void query(Traversable<?, ?> q);
}