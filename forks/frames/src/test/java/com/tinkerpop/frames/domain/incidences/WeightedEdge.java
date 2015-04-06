package com.tinkerpop.frames.domain.incidences;

import com.tinkerpop.frames.EdgeFrame;
import com.tinkerpop.frames.Property;

/**
 * @author Mike Bryant (https://github.com/mikesname)
 */
public interface WeightedEdge extends EdgeFrame {
    @Property("weight")
    public Float getWeight();

    @Property("weight")
    public void setWeight(float weight);
}
