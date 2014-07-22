package org.jboss.windup.reporting.model;

import org.jboss.windup.reporting.model.source.SourceReportModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue(FreeMarkerSourceReportModel.TYPE)
public interface FreeMarkerSourceReportModel extends SourceReportModel
{
    public static final String TYPE = "FreeMarkerSourceReport";

    @JavaHandler
    public String getSourceBlock();

    abstract class Impl implements FreeMarkerSourceReportModel, JavaHandlerContext<Vertex>
    {
        public String getSourceBlock()
        {
            StringBuilder builder = new StringBuilder();

            boolean first = true;
            for (BlackListModel line : getSourceFileModel().getBlackListModels())
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
