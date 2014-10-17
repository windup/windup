package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.service.ClassificationService;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateModelException;

/**
 * Indicates whether or not this file has been marked as "Lift and Shift" (no migration effort necessary).
 * 
 * Example call:
 * 
 * isLiftAndShift(FileModel).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class IsLiftAndShift implements WindupFreeMarkerMethod
{
    private static final String NAME = "isLiftAndShift";

    private GraphContext context;

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
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
        return new ClassificationService(context).isLiftAndShift(fileModel);
    }

}
