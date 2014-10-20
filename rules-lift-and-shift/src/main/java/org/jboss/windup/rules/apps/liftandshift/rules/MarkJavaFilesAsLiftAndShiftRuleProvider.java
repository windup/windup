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
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.rules.apps.liftandshift.constants.ClassificationConstants;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This {@link WindupRuleProvider} finds Java files that have no proprietary references and marks these as "Lift & Shift" (thus requiring no migration
 * effort).
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class MarkJavaFilesAsLiftAndShiftRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = Logging.get(MarkJavaFilesAsLiftAndShiftRuleProvider.class);

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
                    .when(Query.find(JavaSourceFileModel.class))
                    .perform(new MarkLiftAndShiftJavaFilesOperation())
                    .addRule()
                    .when(Query.find(JavaClassFileModel.class))
                    .perform(new MarkLiftAndShiftJavaFilesOperation());
    }

    private class MarkLiftAndShiftJavaFilesOperation extends AbstractIterationOperation<FileModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
        {
            ClassificationService classificationService = new ClassificationService(event.getGraphContext());
            InlineHintService inlineHintService = new InlineHintService(event.getGraphContext());

            boolean markedLiftAndShift = false;
            boolean containsProprietary = false;
            for (ClassificationModel classificationModel : classificationService.getClassificationModelsForFile(payload))
            {
                if (ClassificationConstants.CLASSIFICATION_LIFT_AND_SHIFT.equals(classificationModel.getClassification()))
                {
                    markedLiftAndShift = true;
                }

                if (classificationModel.getContainsProprietaryCode())
                {
                    containsProprietary = true;
                    break;
                }
            }
            int classificationEffort = classificationService.getMigrationEffortPoints(payload);
            int hintEffort = inlineHintService.getMigrationEffortPoints(payload);
            boolean zeroEffort = classificationEffort == 0 || hintEffort == 0;

            if (!markedLiftAndShift && !containsProprietary && zeroEffort)
            {
                LOG.info("Marking Java File as Lift & Shift: " + payload.getFilePath());
                classificationService.attachClassification(payload, ClassificationConstants.CLASSIFICATION_LIFT_AND_SHIFT,
                            ClassificationConstants.CLASSIFICATION_LIFT_AND_SHIFT_DESCRIPTION);
            }
        }
    }
}
