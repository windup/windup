package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.freemarker.FreeMarkerUtil;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.Severity;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetProblemSummariesMethod implements WindupFreeMarkerMethod
{
    public static final String NAME = "getProblemSummaries";

    private GraphContext context;

    @Override
    public String getMethodName()
    {
        return NAME;
    }

    @Override
    public String getDescription()
    {
        return "Returns a summary of all classification and hints found during analysis in the form of a List<"
                    + ProblemSummary.class.getSimpleName() + ">.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException
    {
        // Get the project if one was passed in
        final ProjectModel projectModel;
        if (arguments.size() > 0)
        {
            StringModel projectModelArg = (StringModel) arguments.get(0);
            if (projectModelArg == null)
                projectModel = null;
            else
                projectModel = (ProjectModel) projectModelArg.getWrappedObject();
        }
        else
        {
            projectModel = null;
        }

        Set<String> includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(1));
        Set<String> excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(2));

        Map<Severity, List<ProblemSummary>> problemSummaries = ProblemSummaryService.getProblemSummaries(context, projectModel, includeTags,
                    excludeTags);

        // Convert the keys to String to make Freemarker happy
        Map<String, List<ProblemSummary>> primarySummariesByString = new HashMap<>(problemSummaries.size());
        for (Map.Entry<Severity, List<ProblemSummary>> entry : problemSummaries.entrySet())
        {
            String severityString = entry.getKey() == null ? null : entry.getKey().toString();
            primarySummariesByString.put(severityString, entry.getValue());
        }
        return primarySummariesByString;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }
}
