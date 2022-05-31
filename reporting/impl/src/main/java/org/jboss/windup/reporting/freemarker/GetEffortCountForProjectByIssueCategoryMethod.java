package org.jboss.windup.reporting.freemarker;

import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Gets the number of incidents involved in migrating this application
 * <p>
 * Called from a freemarker template as follows:
 *
 * <pre>
 * getEffortCountForProjectByIssueCategory(GraphRewrite, ProjectModelTraversal, recursive) : Map&lt;String, int&gt;
 * </pre>
 * <p>
 * If recursive is true, the effort total includes child projects.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class GetEffortCountForProjectByIssueCategoryMethod implements WindupFreeMarkerMethod {
    private static final String NAME = "getEffortCountForProjectByIssueCategory";

    private ClassificationService classificationService;
    private InlineHintService inlineHintService;

    @Override
    public void setContext(GraphRewrite event) {
        this.classificationService = new ClassificationService(event.getGraphContext());
        this.inlineHintService = new InlineHintService(event.getGraphContext());
    }

    @Override
    public String getMethodName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return "Parameters are (GraphRewrite, ProjectModelTraversal, [recursive]) and returns Map<String, int> where the key is the Severity and the value is the number of incidents of that severity.";
    }

    @Override
    public Object exec(@SuppressWarnings("rawtypes") List arguments) throws TemplateModelException {
        ExecutionStatistics.get().begin(NAME);
        if (arguments.size() < 3) {
            throw new TemplateModelException(
                    "Error, method expects at least three arguments (event:GraphRewrite, projectModelTraversal:ProjectModelTraversal, recursive:Boolean, [includeTags:Set<String>]. [excludeTags:Set<String>])");
        }

        GraphRewrite event = (GraphRewrite) ((StringModel) arguments.get(0)).getWrappedObject();

        StringModel projectModelTraversalArg = (StringModel) arguments.get(1);
        ProjectModelTraversal traversal = (ProjectModelTraversal) projectModelTraversalArg.getWrappedObject();

        TemplateBooleanModel recursiveBooleanModel = (TemplateBooleanModel) arguments.get(2);
        boolean recursive = recursiveBooleanModel.getAsBoolean();

        Set<String> includeTags = Collections.emptySet();
        if (arguments.size() >= 4) {
            includeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(3));
        }

        Set<String> excludeTags = Collections.emptySet();
        if (arguments.size() >= 5) {
            excludeTags = FreeMarkerUtil.simpleSequenceToSet((SimpleSequence) arguments.get(4));
        }

        Map<IssueCategoryModel, Integer> classificationEffortDetails = classificationService.getMigrationEffortBySeverity(event, traversal, includeTags,
                excludeTags, Collections.emptySet(), recursive);
        Map<IssueCategoryModel, Integer> hintEffortDetails = inlineHintService.getMigrationEffortBySeverity(event, traversal, includeTags, excludeTags,
                Collections.emptySet(), recursive);

        Map<IssueCategoryModel, Integer> results = new TreeMap<>(new IssueCategoryModel.IssueSummaryPriorityComparator());
        addAllIncidents(results, classificationEffortDetails);
        addAllIncidents(results, hintEffortDetails);

        ExecutionStatistics.get().end(NAME);
        return results;
    }

    private void addAllIncidents(Map<IssueCategoryModel, Integer> results, Map<IssueCategoryModel, Integer> effortDetails) {
        for (Map.Entry<IssueCategoryModel, Integer> entry : effortDetails.entrySet()) {
            if (!results.containsKey(entry.getKey()))
                results.put(entry.getKey(), entry.getValue());
            else
                results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
        }
    }
}
