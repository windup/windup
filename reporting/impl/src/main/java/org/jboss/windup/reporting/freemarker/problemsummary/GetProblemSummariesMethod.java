package org.jboss.windup.reporting.freemarker.problemsummary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.freemarker.WindupFreeMarkerMethod;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;

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
        // get all classifications
        // get all hints
        // group them by title (classification and hint title)

        List<ProblemSummary> results = new ArrayList<>();
        Map<RuleSummaryKey, ProblemSummary> ruleIDToSummary = new HashMap<>();

        InlineHintService hintService = new InlineHintService(context);
        for (InlineHintModel hint : hintService.findAll())
        {
            RuleSummaryKey key = new RuleSummaryKey(hint.getRuleID(), hint.getTitle());

            ProblemSummary summary = ruleIDToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(hint.getRuleID(), hint.getTitle(), 1, hint.getEffort());
                ruleIDToSummary.put(key, summary);
                results.add(summary);
            }
            else
            {
                summary.setNumberFound(summary.getNumberFound() + 1);
            }
            summary.addFile(hint.getFile());
        }

        ClassificationService classificationService = new ClassificationService(context);
        for (ClassificationModel classification : classificationService.findAll())
        {
            RuleSummaryKey key = new RuleSummaryKey(classification.getRuleID(), classification.getClassification());
            ProblemSummary summary = ruleIDToSummary.get(key);
            if (summary == null)
            {
                summary = new ProblemSummary(classification.getRuleID(), classification.getClassification(), 0, classification.getEffort());
                ruleIDToSummary.put(key, summary);
                results.add(summary);
            }

            int fileCount = summary.getNumberFound();
            for (FileModel file : classification.getFileModels())
            {
                fileCount++;
                summary.addFile(file);
            }
            summary.setNumberFound(fileCount);
        }

        return results;
    }

    @Override
    public void setContext(GraphRewrite event)
    {
        this.context = event.getGraphContext();
    }

    private class RuleSummaryKey
    {
        private String ruleID;
        private String title;

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
