package org.jboss.windup.tests.application.temporary.performance.data.initialization;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ClassificationModel;

/**
 * Created by mbriskar on 1/15/16.
 */
public class NoiseDataInitializer extends AbstractDataInitializer
{
    private int numberOfNoiseFileModels;
    private int numberOfClassificationNoise;
    private int numberOfHintsNoise;

    public NoiseDataInitializer(int numberOfNoiseFileModels, int numberOfClassificationNoise, int numberOfHintsNoise) {
        this.numberOfNoiseFileModels=numberOfNoiseFileModels;
        this.numberOfClassificationNoise=numberOfClassificationNoise;
        this.numberOfHintsNoise=numberOfHintsNoise;
    }

    @Override public void initData(GraphContext context)
    {
        //noise file models
        context.getGraph().getBaseGraph().commit();
        createFileModelsWithPreffix("prefix", numberOfNoiseFileModels,context);
        context.getGraph().getBaseGraph().commit();
        //noise classifications
        for(int i =0; i< numberOfClassificationNoise; i++) {
            ClassificationModel classificationModel = context.getFramed().addVertex(null, ClassificationModel.class);
            classificationModel.setClassification(i + ". classification");
        }
        context.getGraph().getBaseGraph().commit();
        //noise hints
        for(int i =0; i< numberOfHintsNoise; i++) {
            ClassificationModel classificationModel = context.getFramed().addVertex(null, ClassificationModel.class);
            classificationModel.setClassification(i + ". hint");
        }
        context.getGraph().getBaseGraph().commit();
    }

}
