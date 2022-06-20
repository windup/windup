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
import org.jboss.windup.reporting.category.IssueCategoryModel;

/**
 * Returns a summary of all classification and hints found during analysis in the form of a List&lt;ProblemSummary&gt;.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetProblemSummariesMethod implements WindupFreeMarkerMethod {
    private GraphContext context;

    @Override
    public String getDescription() {
        return "Returns a summary of all classification and hints found during analysis in the form of a List<"
                + ProblemSummary.class.getSimpleName() + ">.";
    }

    @Override
    public Object exec(List arguments) throws TemplateModelException {
        if (arguments.isEmpty())
            throw new TemplateModelException("Method " + getMethodName() + " requires the following parameters (GraphRewrite event, ProjectModel project, Set<String> includeTags, Set<String> excludeTags)");

        // Gets the graph rewrite event
        final GraphRewrite event = (GraphRewrite) ((StringModel) arguments.get(0)).getWrappedObject();

        // Get the project if one was passed in
        final ProjectModel projectModel;

        StringModel projectModelArg = (StringModel) arguments.get(1);
        if (projectModelArg == null)
            projectModel = null;
        else
            projectModel = (ProjectModel) projectModelArg.getWrappedObject();

        Set<String> includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(2));
        Set<String> excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(3));

        Set<ProjectModel> projectModels = getProjects(projectModel);
        Map<IssueCategoryModel, List<ProblemSummary>> problemSummariesOriginal
                = ProblemSummaryService.getProblemSummaries(event.getGraphContext(), projectModels, includeTags, excludeTags);

        // Convert the keys to String to make Freemarker happy
        Comparator<IssueCategoryModel> severityComparator = new IssueCategoryModel.IssueSummaryPriorityComparator();
        Map<IssueCategoryModel, List<ProblemSummary>> problemSummaries = new TreeMap<>(severityComparator);
        problemSummaries.putAll(problemSummariesOriginal);

        Map<String, List<ProblemSummary>> primarySummariesByString = new LinkedHashMap<>(problemSummariesOriginal.size());
        for (Map.Entry<IssueCategoryModel, List<ProblemSummary>> entry : problemSummaries.entrySet()) {
            String severityString = entry.getKey() == null ? null : entry.getKey().getName();
            primarySummariesByString.put(severityString, entry.getValue());
        }

        return primarySummariesByString;
    }

    private Set<ProjectModel> getProjects(ProjectModel projectModel) {
        if (projectModel == null)
            return null;

        ProjectModelTraversal traversal = new ProjectModelTraversal(projectModel, new OnlyOnceTraversalStrategy());
        return traversal.getAllProjects(true);
    }

    @Override
    public void setContext(GraphRewrite event) {
        this.context = event.getGraphContext();
    }
}
