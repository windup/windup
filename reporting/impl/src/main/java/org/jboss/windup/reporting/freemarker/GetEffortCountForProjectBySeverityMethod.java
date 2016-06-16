package org.jboss.windup.reporting.freemarker;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;

/**
 * Gets the number of incidents involved in migrating this application
 *
 * Called from a freemarker template as follows:
 *
 *    <pre>getEffortCountForProjectBySeverity(projectModel, recursive) : int</pre>
 *
 * If recursive is true, the effort total includes child projects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 *
 */
public class GetEffortCountForProjectBySeverityMethod implements WindupFreeMarkerMethod
{
    private static final String NAME = "getEffortCountForProjectBySeverity";

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
                    + " as a parameter and returns Map<String, int> where the key is the Severity and the value is the number of incidents of that severity.";
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
        StringModel projectModelTraversalArg = (StringModel) arguments.get(0);
        ProjectModelTraversal traversal = (ProjectModelTraversal) projectModelTraversalArg.getWrappedObject();

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

        traversal.reset();
        Map<Severity, Integer> classificationEffortDetails = classificationService.getMigrationEffortBySeverity(traversal, includeTags, excludeTags, recursive);
        traversal.reset();
        Map<Severity, Integer> hintEffortDetails = inlineHintService.getMigrationEffortBySeverity(traversal, includeTags, excludeTags, recursive);

        Map<String, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
        addAllIncidents(results, classificationEffortDetails);
        addAllIncidents(results, hintEffortDetails);

        ExecutionStatistics.get().end(NAME);
        return results;
    }

    private void addAllIncidents(Map<String, Integer> results, Map<Severity, Integer> effortDetails)
    {
        for (Map.Entry<Severity, Integer> entry : effortDetails.entrySet())
        {
            if (!results.containsKey(entry.getKey().toString()))
                results.put(entry.getKey().toString(), entry.getValue());
            else
                results.put(entry.getKey().toString(), results.get(entry.getKey().toString()) + entry.getValue());
        }
    }
}
