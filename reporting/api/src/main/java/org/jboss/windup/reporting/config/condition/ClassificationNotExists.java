package org.jboss.windup.reporting.config.condition;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.rules.files.model.FileReferenceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Filter out the locations not pointing to a file with classification
 */
public class ClassificationNotExists extends ClassificationExists
{
    private ClassificationNotExists(String messagePattern)
    {
        super(messagePattern);
    }

    @Override
    public boolean accept(GraphRewrite event, EvaluationContext context, WindupVertexFrame payload)
    {
        return !super.accept(event, context, payload);
    }

    /**
     * Specifies the regular expression to use when searching {@link ClassificationModel} entries.
     */
    public static ClassificationNotExists withClassification(String messagePattern)
    {
        return new ClassificationNotExists(messagePattern);
    }
}
