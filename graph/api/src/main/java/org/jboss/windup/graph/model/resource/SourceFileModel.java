package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a file is source code (as opposed to a binary file of some kind).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@TypeValue(SourceFileModel.TYPE)
public interface SourceFileModel extends WindupVertexFrame
{
    public static final String TYPE = "SourceFileModel";

}
