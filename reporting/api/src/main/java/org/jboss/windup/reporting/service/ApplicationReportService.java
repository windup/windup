package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This class provides helpful utility methods for creating and finding {@link ApplicationReportModel} vertices.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ApplicationReportService extends GraphService<ApplicationReportModel>
{
    public ApplicationReportService(GraphContext context)
    {
        super(context, ApplicationReportModel.class);
    }

    /**
     * Overrides GraphService.create() to create the object with some reasonable defaults
     */
    public ApplicationReportModel create()
    {
        ApplicationReportModel applicationReportModel = super.create();
        applicationReportModel.setDisplayInApplicationList(false);
        applicationReportModel.setDisplayInApplicationReportIndex(false);
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setReportPriority(Integer.MAX_VALUE);
        return applicationReportModel;
    }

    public ApplicationReportModel getMainApplicationReportForFile(FileModel fileModel)
    {
        GremlinPipeline<Vertex, Vertex> pipe = new GremlinPipeline<>(getGraphContext().getGraph());
        pipe.V(WindupVertexFrame.TYPE_PROP, ApplicationReportModel.TYPE);
        pipe.has(ApplicationReportModel.MAIN_APPLICATION_REPORT, true);
        pipe.as("applicationReport");
        pipe.out(ApplicationReportModel.REPORT_TO_PROJECT_MODEL);

        // check that the project for this application report is the same as the root project for the provided fileModel
        ProjectModel rootProjectModel = fileModel.getProjectModel();
        if (rootProjectModel == null)
        {
            return null;
        }
        while (rootProjectModel.getParentProject() != null)
        {
            rootProjectModel = rootProjectModel.getParentProject();
        }
        String rootFilePath = rootProjectModel.getRootFileModel().getFilePath();
        pipe.out(ProjectModel.ROOT_FILE_MODEL);
        pipe.has(FileModel.FILE_PATH, rootFilePath);

        pipe.back("applicationReport");

        if (pipe.iterator().hasNext())
        {
            Vertex v = pipe.iterator().next();
            ApplicationReportModel mainAppReport = frame(v);
            return mainAppReport;
        }
        return null;
    }
}
