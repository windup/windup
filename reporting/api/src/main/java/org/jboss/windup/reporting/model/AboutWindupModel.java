package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(AboutWindupModel.TYPE)
public interface AboutWindupModel extends WindupVertexFrame
{
    public static final String TYPE = "AboutWindupModel";
    public static final String WINDUP_RUNTIME_VERSION = "windupRuntimeVersion";

    /**
     * This is the version of Windup that the Windup Report was generated from.
     */
    @Property(WINDUP_RUNTIME_VERSION)
    public String getWindupRuntimeVersion();

    /**
     * This is the version of Windup that the Windup Report was generated from.
     */
    @Property(WINDUP_RUNTIME_VERSION)
    public void setWindupRuntimeVersion(String version);

}
