package org.jboss.windup.rules.apps.liftandshift.service;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;

public class LiftAndShiftService extends ClassificationService
{
    public static final String CLASSIFICATION_LIFT_AND_SHIFT = "Lift and Shift";
    public static final String CLASSIFICATION_LIFT_AND_SHIFT_DESCRIPTION = "(Zero Migration Required)";

    public static final String CLASSIFICATION_CONTAINS_PROPRIETARY = "Contains Proprietary Code";
    public static final String CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION = "Migration Effort Required (proprietary code detected)";

    public LiftAndShiftService(GraphContext context)
    {
        super(context);
    }

    /**
     * Returns true "if and only if" the {@link FileModel} has attached {@link ClassificationModel}s and all of those classifications indicate that it
     * is a "lift and shift" file.
     */
    public boolean isLiftAndShift(FileModel fileModel)
    {
        Iterable<ClassificationModel> classificationModels = getClassificationModelsForFile(fileModel, CLASSIFICATION_LIFT_AND_SHIFT);
        return classificationModels.iterator().hasNext();
    }

    public final ClassificationModel getContainsProprietaryClassification()
    {
        ClassificationModel classificationModel = getByClassification(CLASSIFICATION_CONTAINS_PROPRIETARY);
        if (classificationModel == null)
        {
            classificationModel = create();
            classificationModel.setDescription(CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION);
            classificationModel.setEffort(1);
        }
        return classificationModel;
    }

    public final ClassificationModel getLiftAndShiftClassification(GraphRewrite event)
    {
        ClassificationModel classificationModel = getByClassification(CLASSIFICATION_CONTAINS_PROPRIETARY);
        if (classificationModel == null)
        {
            classificationModel = create();
            classificationModel.setDescription(CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION);
            classificationModel.setEffort(0);
        }
        return classificationModel;
    }
}
