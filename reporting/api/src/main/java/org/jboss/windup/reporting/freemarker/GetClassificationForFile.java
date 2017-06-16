package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Gets the classification for the provided {@link FileModel}.
 * 
 * Example call:
 * 
 * getClassificationForFile(FileModel)
 * 
 * The method will return an Iterable containing {@link ClassificationModel} instances.
 * 
 */
public class GetClassificationForFile implements WindupFreeMarkerMethod
{
    private static final String NAME = "getClassificationForFile";
    private GraphContext context;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 1)
        {
            throw new TemplateModelException("Error, method expects one argument (" + FileModel.class.getSimpleName() + ")");
        }
        StringModel stringModelArg = (StringModel) arguments.get(0);
        FileModel fileModel = (FileModel) stringModelArg.getWrappedObject();
        
        ClassificationService classificationService = new ClassificationService(context);
        Iterable<ClassificationModel> result = classificationService.getClassifications(fileModel);
        
        ExecutionStatistics.get().end(NAME);
        return result;
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + FileModel.class.getSimpleName()
                    + " as a parameter and returns an Iterable<" + ClassificationModel.class.getSimpleName()
                    + "> containing the classification for this file.";
    }
}
