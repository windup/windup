package org.jboss.windup.graph.model.resource;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
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

    /**
     * Returns the path of this file within the parent project (format suitable for reporting)
     * Uses fully qualified class name notation for classes
     */
    @JavaHandler
    String getPrettyPathWithinProject(boolean useFQNForClasses);

    abstract class Impl extends FileModel.Impl implements ReportResourceFileModel, JavaHandlerContext<Vertex>
    {
        @Override
        public String getPrettyPathWithinProject(boolean useFQNForClasses)
        {
            // TODO: Fix this
            return "resources/" + this.getPrettyPath();
        }
    }
}
