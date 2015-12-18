package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;

import freemarker.ext.beans.StringModel;
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

        // get all classifications
        // get all hints
        // group them by title (classification and hint title)

        // The key is the severity as a String
        Map<String, List<ProblemSummary>> results = new TreeMap<>(new Comparator<String>()
        {
            @Override
            public int compare(String severity1, String severity2)
            {
                return Severity.valueOf(severity1).ordinal() - Severity.valueOf(severity2).ordinal();
            }
        });
        Map<RuleSummaryKey, ProblemSummary> ruleIDToSummary = new HashMap<>();

        InlineHintService hintService = new InlineHintService(context);
        final Iterable<InlineHintModel> hints = projectModel == null ? hintService.findAll() : hintService.getHintsForProject(projectModel, true);
        for (InlineHintModel hint : hints)
        {
            RuleSummaryKey key = new RuleSummaryKey(hint.getRuleID(), hint.getTitle());

            ProblemSummary summary = ruleIDToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(hint.getSeverity().toString(), hint.getRuleID(), hint.getTitle(), 1, hint.getEffort());
                ruleIDToSummary.put(key, summary);
                addToResults(results, summary);
            }
            else
            {
                summary.setNumberFound(summary.getNumberFound() + 1);
            }
            summary.addFile(hint.getHint(), hint.getFile());
        }

        ClassificationService classificationService = new ClassificationService(context);
        for (ClassificationModel classification : classificationService.findAll())
        {
            List<FileModel> newFileModels = new ArrayList<>();
            for (FileModel file : classification.getFileModels())
            {
                if (projectModel != null)
                {
                    // make sure this one is in the project
                    boolean projectMatches = false;
                    ProjectModel fileProject = file.getProjectModel();
                    while (fileProject != null)
                    {
                        if (fileProject.equals(projectModel))
                        {
                            projectMatches = true;
                            break;
                        }
                        fileProject = fileProject.getParentProject();
                    }
                    if (!projectMatches)
                        continue;
                }
                newFileModels.add(file);
            }

            if (newFileModels.isEmpty())
                continue;

            RuleSummaryKey key = new RuleSummaryKey(classification.getRuleID(), classification.getClassification());
            ProblemSummary summary = ruleIDToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(classification.getSeverity().toString(), classification.getRuleID(), classification.getClassification(),
                            0,
                            classification.getEffort());
                ruleIDToSummary.put(key, summary);
                addToResults(results, summary);
            }

            for (FileModel file : newFileModels)
                summary.addFile(classification.getDescription(), file);

            summary.setNumberFound(summary.getNumberFound() + newFileModels.size());
        }

        return results;
    }

    private void addToResults(Map<String, List<ProblemSummary>> results, ProblemSummary summary)
    {
        List<ProblemSummary> list = results.get(summary.getSeverity());
        if (list == null)
        {
            list = new ArrayList<>();
            results.put(summary.getSeverity(), list);
        }
        list.add(summary);
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    private class RuleSummaryKey
    {
        private final String ruleID;
        private final String title;

        public RuleSummaryKey(String ruleID, String title)
        {
            this.ruleID = ruleID;
            this.title = title;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RuleSummaryKey that = (RuleSummaryKey) o;

            if (ruleID != null ? !ruleID.equals(that.ruleID) : that.ruleID != null)
                return false;
            return !(title != null ? !title.equals(that.title) : that.title != null);

        }

        @Override
        public int hashCode()
        {
            int result = ruleID != null ? ruleID.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            return result;
        }
    }
}
