package org.jboss.windup.rules.apps.liftandshift.constants;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;

public class ClassificationConstants
{
    public static final String CLASSIFICATION_LIFT_AND_SHIFT = "Lift and Shift";
    public static final String CLASSIFICATION_LIFT_AND_SHIFT_DESCRIPTION = "(Zero Migration Required)";

    public static final String CLASSIFICATION_CONTAINS_PROPRIETARY = "Contains Proprietary Code";
    public static final String CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION = "Migration Effort Required (proprietary code detected)";

    /**
     * Returns true "if and only if" the {@link FileModel} has attached {@link ClassificationModel}s and all of those classifications indicate that it
     * is a "lift and shift" file.
     */
    public static boolean isLiftAndShift(GraphRewrite event, FileModel fileModel)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());

        Iterable<ClassificationModel> classificationModels = classificationService.getClassificationModelsForFile(fileModel);
        for (ClassificationModel cm : classificationModels)
        {
            if (CLASSIFICATION_LIFT_AND_SHIFT.equals(cm.getClassification()))
            {
                return true;
            }
        }

        return false;
    }

    public static final ClassificationModel getContainsProprietaryClassification(GraphRewrite event)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel classificationModel = classificationService.getByClassification(CLASSIFICATION_CONTAINS_PROPRIETARY);
        if (classificationModel == null)
        {
            classificationModel = classificationService.create();
            classificationModel.setDescription(CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION);
            classificationModel.setEffort(1);
        }
        return classificationModel;
    }

    public static final ClassificationModel getLiftAndShiftClassification(GraphRewrite event)
    {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel classificationModel = classificationService.getByClassification(CLASSIFICATION_CONTAINS_PROPRIETARY);
        if (classificationModel == null)
        {
            classificationModel = classificationService.create();
            classificationModel.setDescription(CLASSIFICATION_CONTAINS_PROPRIETARY_DESCRIPTION);
            classificationModel.setEffort(0);
        }
        return classificationModel;
    }

    private ClassificationConstants()
    {
    }
}
