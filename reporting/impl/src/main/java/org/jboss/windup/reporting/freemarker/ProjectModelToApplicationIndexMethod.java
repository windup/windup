package org.jboss.windup.reporting.freemarker;

import java.util.List;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * 
 * Given a {@link ProjectModel}, return the {@link ApplicationReportIndexModel} that is associated with the application.
 * 
 * The function takes one parameter, and can be called from a freemarker template as follows:
 * 
 * projectModelToApplicationIndex(projectModel)
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class ProjectModelToApplicationIndexMethod implements WindupFreeMarkerMethod
{
    private static Logger LOG = Logging.get(ProjectModelToApplicationIndexMethod.class);

    private static final String NAME = "projectModelToApplicationIndex";

    private ApplicationReportIndexService service;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.service = new ApplicationReportIndexService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a parameter of type " + ProjectModel.class.getSimpleName() + " and returns the associated "
                    + ApplicationReportIndexModel.class.getSimpleName() + ".";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (ProjectModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        if (stringModelArg == null)
        {
            throw new IllegalArgumentException("FreeMarker Method " + NAME + " called with null project model");
        }
        ProjectModel projectModel = (ProjectModel) stringModelArg.getWrappedObject();
        ApplicationReportIndexModel index = service.getApplicationReportIndexForProjectModel(projectModel);
        if (index == null)
        {
            LOG.warning("Could not find an application index for project model: " + projectModel.getName() + " (Vertex ID: "
                        + projectModel.asVertex().getId() + ")");
        }
        ExecutionStatistics.get().end(NAME);
        return index;
    }
}
