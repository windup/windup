package org.jboss.windup.graph.model;

import com.syncleus.ferma.EdgeFrame;
import org.apache.tinkerpop.gremlin.structure.Edge;

/**
 * The base {@link EdgeFrame} type implemented by all model types.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupEdgeFrame extends EdgeFrame, WindupFrame<Edge>
{
}
