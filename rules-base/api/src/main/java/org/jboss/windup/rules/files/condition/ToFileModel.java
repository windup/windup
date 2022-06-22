package org.jboss.windup.rules.files.condition;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.iterables.FramesSetIterable;
import org.jboss.windup.graph.model.ToFileModelTransformable;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.annotation.Nullable;

/**
 * Condition transforming the input iterable of {@link ToFileModelTransformable} instances into {@link FileModel}s.
 */
public class ToFileModel extends GraphCondition {
    private GraphCondition wrappedCondition;

    public ToFileModel() {
    }

    public static ToFileModel withWrappedCondition(GraphCondition condition) {
        ToFileModel toFileModelCondition = new ToFileModel();
        toFileModelCondition.wrappedCondition = condition;
        return toFileModelCondition;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context) {
        wrappedCondition.evaluate(event, context);
        Iterable<? extends WindupVertexFrame> vertices = Variables.instance(event).findVariable(wrappedCondition.getOutputVariablesName());
        Iterable<FileModel> resultIterable = FluentIterable.from(vertices).transformAndConcat(new Function<WindupVertexFrame, Iterable<FileModel>>() {
            @Nullable
            @Override
            public Iterable<FileModel> apply(WindupVertexFrame windupVertexFrame) {
                if (!(windupVertexFrame instanceof ToFileModelTransformable)) {
                    throw new WindupException("ToFileModel may work only with the objects that implements ToFileModelTransformable interface");
                }
                return ((ToFileModelTransformable) windupVertexFrame).transformToFileModel();
            }
        });
        Variables.instance(event).setVariable(getOutputVariablesName(), new FramesSetIterable(resultIterable));
        return resultIterable.iterator().hasNext();
    }

}
