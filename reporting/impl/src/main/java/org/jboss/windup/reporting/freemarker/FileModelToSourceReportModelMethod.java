package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportModelService;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * 
 * This FreeMarker method simply finds the SourceReport that is associated with the provided FileModel, if there is a SourceReport available.
 * 
 * If none is available, it will return null.
 * 
 * The function takes one parameter, and can be called from a freemarker template as follows:
 * 
 * fileModelToSourceReport(fileModel)
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class FileModelToSourceReportModelMethod implements WindupFreeMarkerMethod
{
    public static final String METHOD_NAME = "fileModelToSourceReport";
    private SourceReportModelService sourceReportService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.sourceReportService = new SourceReportModelService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return METHOD_NAME;
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (FileModel)");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();
        SourceReportModel result = sourceReportService
                    .getSourceReportForFileModel(fileModel);
        return result;
    }

}
