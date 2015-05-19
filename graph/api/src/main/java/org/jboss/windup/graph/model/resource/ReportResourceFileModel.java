package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a file is binary (such as image).
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@TypeValue(ReportResourceFileModel.TYPE)
public interface ReportResourceFileModel extends FileModel
{
    public static final String TYPE = "RawFileModel";

}
