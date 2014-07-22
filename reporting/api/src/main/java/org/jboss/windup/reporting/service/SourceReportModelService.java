package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.source.SourceReportModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class SourceReportModelService extends GraphService<SourceReportModel>
{

    public SourceReportModelService()
    {
        super(SourceReportModel.class);
    }

    public SourceReportModelService(GraphContext context)
    {
        super(context, SourceReportModel.class);
    }

    public SourceReportModel getSourceReportForFileModel(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(fileModel.asVertex());
        pipeline.in(SourceReportModel.SOURCE_REPORT_TO_SOURCE_FILE_MODEL);

        SourceReportModel result = null;
        if (pipeline.hasNext())
        {
            result = frame(pipeline.next());
        }
        return result;
    }
}
