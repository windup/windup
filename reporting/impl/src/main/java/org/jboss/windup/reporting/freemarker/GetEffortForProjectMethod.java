package org.jboss.windup.reporting.freemarker;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
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
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
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
        if (arguments.size() < 2)
        {
            throw new TemplateModelException(
                        "Error, method expects at least two arguments (projectModel:ProjectModel, recursive:Boolean, [includeTags:Set<String>]. [excludeTags:Set<String>])");
        }
        StringModel projectModelArg = (StringModel) arguments.get(0);
        ProjectModel projectModel = (ProjectModel) projectModelArg.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) arguments.get(1);
        boolean recursive = recursiveBooleanModel.getAsBoolean();

        Set<String> includeTags = Collections.emptySet();
        if (arguments.size() >= 3)
        {
            includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(2));
        }

        Set<String> excludeTags = Collections.emptySet();
        if (arguments.size() >= 4)
        {
            excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(3));
        }

        Object result = classificationService.getMigrationEffortPoints(projectModel, includeTags, excludeTags, recursive)
                    + inlineHintService.getMigrationEffortPoints(projectModel, includeTags, excludeTags, recursive);
        ExecutionStatistics.get().end(NAME);
        return result;
    }
}
