package org.jboss.windup.graph.model.resource;

import java.io.File;
import java.io.InputStream;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("BaseResource")
public interface ResourceModel extends WindupVertexFrame
{
    public InputStream asInputStream();

    public File asFile() throws RuntimeException;
}
