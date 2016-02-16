package org.jboss.windup.reporting.service;

import com.tinkerpop.blueprints.Vertex;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface EffortAccumulatorFunction
{
    void accumulate(Vertex effortReportVertex);
}
