package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.source.SourceReportModel;

/**
 * This provides helper queries and functions for finding and creating SourceReportModel instances.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class SourceReportService extends GraphService<SourceReportModel> {
    public SourceReportService(GraphContext context) {
        super(context, SourceReportModel.class);
    }

    /**
     * Find the SourceReportModel instance for this fileModel (this is a 1:1 relationship).
     */
    public SourceReportModel getSourceReportForFileModel(FileModel fileModel) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(fileModel.getElement());
        pipeline.in(SourceReportModel.SOURCE_REPORT_TO_SOURCE_FILE_MODEL);

        SourceReportModel result = null;
        if (pipeline.hasNext()) {
            result = frame(pipeline.next());
        }
        return result;
    }
}
