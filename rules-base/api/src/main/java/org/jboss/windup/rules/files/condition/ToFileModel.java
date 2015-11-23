package org.jboss.windup.rules.files.condition;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.rules.files.org.jboss.windup.rules.general.IterableFilter;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.annotation.Nullable;

/**
 * Created by mbriskar on 11/19/15.
 */
public class ToFileModel extends GraphCondition
{
    private GraphCondition wrappedCondition;
    private Integer size;

    public static IterableFilter withSize(int size) {
        return new IterableFilter(size);
    }

    public ToFileModel() {
    }

    public void withWrappedCondition(GraphCondition condition)
    {
        this.wrappedCondition=condition;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        wrappedCondition.evaluate(event,context);
        Iterable<? extends WindupVertexFrame> vertices= Variables.instance(event).findVariable(wrappedCondition.getOutputVariablesName());
        Iterable<FileModel> resultIterable = FluentIterable.from(vertices).transformAndConcat(new Function<WindupVertexFrame, Iterable<FileModel>>()
        {
            @Nullable @Override
            public Iterable<FileModel> apply(WindupVertexFrame windupVertexFrame)
            {
                if (!(windupVertexFrame instanceof ToFileModelTransformable))
                {
                    throw new WindupException("ToFileModel may work only with the objects that may be transformed to FileModels");
                }
                return ((ToFileModelTransformable) windupVertexFrame).transformToFileModel();
            }
        });
        Variables.instance(event).setVariable(getOutputVariablesName(),resultIterable);
        return resultIterable.iterator().hasNext();
    }
}
