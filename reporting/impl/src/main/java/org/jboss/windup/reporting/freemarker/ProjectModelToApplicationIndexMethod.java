package org.jboss.windup.reporting.freemarker;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.ApplicationReportIndexModel;
import org.jboss.windup.reporting.service.ApplicationReportIndexService;

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

    private static final String PROJECT_MODEL_TO_APPLICATION_INDEX = "projectModelToApplicationIndex";

    @Inject
    private ApplicationReportIndexService service;

    @Override
    public String getMethodName()
    {
        return PROJECT_MODEL_TO_APPLICATION_INDEX;
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (ProjectModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ProjectModel projectModel = (ProjectModel) stringModelArg.getWrappedObject();
        ApplicationReportIndexModel index = service.getApplicationReportIndexForProjectModel(projectModel);
        return index;
    }
}
