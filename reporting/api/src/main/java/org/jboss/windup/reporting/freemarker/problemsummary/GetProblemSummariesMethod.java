package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.traversal.OnlyOnceTraversalStrategy;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.freemarker.FreeMarkerUtil;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModelException;
import org.jboss.windup.reporting.severity.IssueCategoryModel;

/**
 * Returns a summary of all classification and hints found during analysis in the form of a List&lt;ProblemSummary&gt;.
 *
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
        if (arguments.size() == 0)
            throw new TemplateModelException("Method " + NAME + " requires the following parameters (GraphRewrite event, [ProjectModel project])");

        // Gets the graph rewrite event
        final GraphRewrite event = (GraphRewrite)((StringModel)arguments.get(0)).getWrappedObject();

        // Get the project if one was passed in
        final ProjectModel projectModel;
        if (arguments.size() > 1)
        {
            StringModel projectModelArg = (StringModel) arguments.get(1);
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

        Set<ProjectModel> projectModels = getProjects(projectModel);
        Map<IssueCategoryModel, List<ProblemSummary>> problemSummariesOriginal = ProblemSummaryService.getProblemSummaries(event, projectModels, includeTags,
                    excludeTags);

        // Convert the keys to String to make Freemarker happy
        Comparator<IssueCategoryModel> severityComparator = new Comparator<IssueCategoryModel>()
        {
            @Override
            public int compare(IssueCategoryModel severity1, IssueCategoryModel severity2)
            {
                int ordinal1 = severity1 == null ? 0 : severity1.getPriority();
                int ordinal2 = severity2 == null ? 0 : severity2.getPriority();

                return ordinal1 - ordinal2;
            }
        };
        Map<IssueCategoryModel, List<ProblemSummary>> problemSummaries = new TreeMap<>(severityComparator);
        problemSummaries.putAll(problemSummariesOriginal);

        Map<String, List<ProblemSummary>> primarySummariesByString = new LinkedHashMap<>(problemSummariesOriginal.size());
        for (Map.Entry<IssueCategoryModel, List<ProblemSummary>> entry : problemSummaries.entrySet())
        {
            String severityString = entry.getKey() == null ? null : entry.getKey().toString();
            primarySummariesByString.put(severityString, entry.getValue());
        }

        return primarySummariesByString;
    }

    private Set<ProjectModel> getProjects(ProjectModel projectModel)
    {
        if (projectModel == null)
            return null;

        ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy());
        return traversal.getAllProjects(true);
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }
}
