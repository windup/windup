package org.jboss.windup.reporting.model;

import org.jboss.windup.reporting.model.source.SourceReportModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * This extends SourceReportModel with some functions that are used by our source rendering template.
 * 
 */
@TypeValue(FreeMarkerSourceReportModel.TYPE)
public interface FreeMarkerSourceReportModel extends SourceReportModel
{
    public static final String TYPE = "FreeMarkerSourceReport";

    /**
     * This is used by the Javascript in the source rendering template to provide code assist blocks in the rendered
     * output.
     */
    @JavaHandler
    public String getSourceBlock();

    abstract class Impl implements FreeMarkerSourceReportModel, JavaHandlerContext<Vertex>
    {
        public String getSourceBlock()
        {
            StringBuilder builder = new StringBuilder();

            boolean first = true;
            for (InlineHintModel line : getSourceFileModel().getInlineHints())
            {
                if (!first)
                {
                    builder.append(",");
                }
                builder.append(line.getLineNumber());

                if (first)
                {
                    first = false;
                }
            }

            return builder.toString();
        }
    }
}
