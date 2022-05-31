package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.util.Logging;

import java.util.logging.Logger;

/**
 * This class provides helpful utility methods for creating and finding {@link ApplicationReportModel} vertices.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ApplicationReportService extends GraphService<ApplicationReportModel> {
    private static final Logger LOG = Logging.get(ApplicationReportService.class);

    public ApplicationReportService(GraphContext context) {
        super(context, ApplicationReportModel.class);
    }

    /**
     * Overrides GraphService.create() to create the object with some reasonable defaults
     */
    public ApplicationReportModel create() {
        ApplicationReportModel applicationReportModel = super.create();
        applicationReportModel.setDisplayInApplicationReportIndex(false);
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setReportPriority(Integer.MAX_VALUE);
        return applicationReportModel;
    }

    /**
     * Takes the first {@link ApplicationReportModel} that has set boolean value {@link ApplicationReportModel#MAIN_APPLICATION_REPORT} to true and whose
     * projectModel is the same as the rootProjectModel of the given file
     *
     * @param fileModel A FileModel for which we are looking for the main application report to link to.
     * @return
     */
    public ApplicationReportModel getMainApplicationReportForFile(FileModel fileModel) {
        ProjectModel rootProjectModel = fileModel.getProjectModel();
        if (rootProjectModel == null) {
            return null;
        } else {
            rootProjectModel = rootProjectModel.getRootProjectModel();
        }
        GraphTraversal<Vertex, Vertex> pipe = new GraphTraversalSource(getGraphContext().getGraph()).V(rootProjectModel.getElement());
        pipe.in(ApplicationReportModel.REPORT_TO_PROJECT_MODEL);
        pipe.has(ApplicationReportModel.MAIN_APPLICATION_REPORT, true);

        ApplicationReportModel mainAppReport = null;
        for (Vertex v : pipe.toList()) {
            ApplicationReportModel appReport = frame(v);

            if (mainAppReport != null) {
                LOG.warning("There are multiple ApplicationReportModels for a single file " + fileModel.getFilePath() + ". This may cause some broken"
                        + "links in the report file");
            }
            mainAppReport = appReport;
        }
        return mainAppReport;
    }
}
