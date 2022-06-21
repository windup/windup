package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.model.ApplicationReportModel;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Service methods for finding and creating {@link ApplicationReportIndexModel} objects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class ApplicationReportIndexService extends GraphService<ApplicationReportIndexModel> {
    public ApplicationReportIndexService(GraphContext context) {
        super(context, ApplicationReportIndexModel.class);
    }

    /**
     * Return a global application index (not associated with a specific {@link ProjectModel}).
     */
    public ApplicationReportIndexModel getOrCreateGlobalApplicationIndex() {
        GraphTraversal<Vertex, Vertex> pipeline = getGraphContext().getGraph().traversal().V();
        pipeline.has(WindupVertexFrame.TYPE_PROP, ApplicationReportModel.TYPE);
        pipeline.filter(it -> !it.get().edges(Direction.OUT, ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL).hasNext());

        final ApplicationReportIndexModel result = pipeline.hasNext() ? frame(pipeline.next()) : create();
        return result;
    }

    /**
     * Returns the {@link ApplicationReportIndexModel} associated with the provided ProjectModel
     */
    public ApplicationReportIndexModel getApplicationReportIndexForProjectModel(ProjectModel projectModel) {
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(getGraphContext().getGraph()).V(projectModel.getElement());
        pipeline.in(ApplicationReportIndexModel.APPLICATION_REPORT_INDEX_TO_PROJECT_MODEL);

        ApplicationReportIndexModel applicationReportIndex = null;
        if (pipeline.hasNext()) {
            applicationReportIndex = frame(pipeline.next());
        }
        return applicationReportIndex;
    }
}
