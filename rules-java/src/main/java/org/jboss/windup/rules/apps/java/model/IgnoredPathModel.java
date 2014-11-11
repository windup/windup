package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(IgnoredPathModel.TYPE)
public interface IgnoredPathModel extends WindupVertexFrame
{

    public static final String TYPE = "IgnoredFile";

    @Property("name_regex")
    public String getRegex();

    @Property("name_regex")
    public void setRegex(String regex);
    
}
