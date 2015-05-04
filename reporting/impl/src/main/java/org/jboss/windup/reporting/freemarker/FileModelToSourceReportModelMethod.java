package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.ResourceModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * 
 * This FreeMarker method simply finds the SourceReport that is associated with the provided ResourceModel, if there is a SourceReport available.
 * 
 * If none is available, it will return null.
 * 
 * The function takes one parameter, and can be called from a freemarker template as follows:
 * 
 * fileModelToSourceReport(fileModel)
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * 
 */
public class FileModelToSourceReportModelMethod implements WindupFreeMarkerMethod
{
    public static final String NAME = "fileModelToSourceReport";
    private SourceReportService sourceReportService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.sourceReportService = new SourceReportService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ResourceModel.class.getSimpleName() + " as a parameter, and returns the related "
                    + SourceReportModel.class.getSimpleName()
                    + " (or null if none is available).";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (ResourceModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        ResourceModel fileModel = (ResourceModel) stringModelArg.getWrappedObject();
        SourceReportModel result = sourceReportService.getSourceReportForResourceModel(fileModel);
        ExecutionStatistics.get().end(NAME);
        return result;
    }

}
