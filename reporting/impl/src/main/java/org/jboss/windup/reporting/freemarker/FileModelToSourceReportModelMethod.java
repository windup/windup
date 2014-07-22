package org.jboss.windup.reporting.freemarker;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.source.SourceReportModel;
import org.jboss.windup.reporting.service.SourceReportModelService;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

public class FileModelToSourceReportModelMethod implements WindupFreeMarkerMethod
{
    public static final String METHOD_NAME = "fileModelToSourceReport";

    @Inject
    private GraphContext graphContext;

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
        SourceReportModel result = new SourceReportModelService(graphContext).getSourceReportForFileModel(fileModel);
        return result;
    }

}
