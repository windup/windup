package org.jboss.windup.reporting.freemarker;

import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;

/**
 * Gets the number of effort points involved in migrating this application
 * 
 * Called from a freemarker template as follows:
 * 
 * getMigrationEffortPoints(projectModel, recursive):int
 * 
 * If recursive is true, the effort total includes child projects.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class GetEffortForProjectMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "getMigrationEffortPoints";
    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    @Override
    public void setContext(GraphRewrite event)
    {
        this.classificationService = new ClassificationService(event.getGraphContext());
        this.inlineHintService = new InlineHintService(event.getGraphContext());
    }

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Takes a " + ProjectModel.class.getSimpleName()
                    + " as a parameter and returns an int containing the effort estimate for this project.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException
    {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() != 2)
        {
            throw new TemplateModelException(
                        "Error, method expects two arguments (projectModel:ProjectModel, recursive:Boolean)");
        }
        StringModel projectModelArg = (StringModel) arguments.get(0);
        ProjectModel projectModel = (ProjectModel) projectModelArg.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) arguments.get(1);
        boolean recursive = recursiveBooleanModel.getAsBoolean();

        Object result = classificationService.getMigrationEffortPoints(projectModel, recursive)
                    + inlineHintService.getMigrationEffortPoints(projectModel, recursive);
        ExecutionStatistics.get().end(NAME);
        return result;
    }
}
