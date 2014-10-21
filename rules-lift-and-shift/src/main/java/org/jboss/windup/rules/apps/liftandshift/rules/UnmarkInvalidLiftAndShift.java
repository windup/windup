package org.jboss.windup.rules.apps.liftandshift.rules;

import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.liftandshift.service.LiftAndShiftService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Unmarks files that have been classified as "Lift & Shift" if those same files have >0 effort associated with them.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class UnmarkInvalidLiftAndShift extends WindupRuleProvider
{
    private static final Logger LOG = Logging.get(UnmarkInvalidLiftAndShift.class);

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.POST_MIGRATION_RULES;
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.find(FileModel.class))
                    .perform(new UnmarkInvalidLiftAndShiftOperation());
    }

    private class UnmarkInvalidLiftAndShiftOperation extends AbstractIterationOperation<FileModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
        {
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            InlineHintService inlineHintService = new InlineHintService(event.getGraphContext());

            ClassificationModel liftAndShiftModel = null;
            for (ClassificationModel classificationModel : classificationService.getClassificationModelsForFile(payload))
            {
                if (LiftAndShiftService.CLASSIFICATION_LIFT_AND_SHIFT.equals(classificationModel.getClassification()))
                {
                    liftAndShiftModel = classificationModel;
                    break;
                }
            }
            int classificationEffort = classificationService.getMigrationEffortPoints(payload);
            int hintEffort = inlineHintService.getMigrationEffortPoints(payload);
            boolean zeroEffort = classificationEffort == 0 || hintEffort == 0;

            if (liftAndShiftModel != null && !zeroEffort)
            {
                StringBuilder infoString = new StringBuilder();
                infoString.append("File (").append(payload.getFilePath() + ") marked as Lift & Shift, however, it is not a zero effort file.\n");
                infoString.append("\tAssociated classifications and hints: ");
                for (ClassificationModel cm : classificationService.getClassificationModelsForFile(payload))
                {
                    infoString.append("\t\tClassification: ").append(cm.getClassification()).append(", effort: ")
                                .append(cm.getEffort()).append(", rule: ").append(cm.getRuleID()).append("\n");
                }
                for (InlineHintModel hint : inlineHintService.getHintsForFile(payload))
                {
                    infoString.append("\t\tHint Line #: ").append(hint.getLineNumber()).append(", effort: ")
                                .append(hint.getEffort()).append(" rule: ").append(hint.getRuleID()).append("\n");
                }
                infoString.append("\tRemoving invalid marker!");
                LOG.warning(infoString.toString());
                classificationService.delete(liftAndShiftModel);
            }
        }
    }

}
