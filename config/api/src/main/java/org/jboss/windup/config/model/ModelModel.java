package org.jboss.windup.config.model;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Model for Models - to have the information about models in the graph.
 * This serves e.g. for reporting.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeValue("ModelModel")
public interface ModelModel extends WindupVertexFrame
{
    public static final String CLASS = "class";

    @Property(CLASS)
    public void setClassName(String name);

    @Property(CLASS)
    public String getClassName();
}
