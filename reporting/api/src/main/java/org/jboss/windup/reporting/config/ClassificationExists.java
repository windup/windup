package org.jboss.windup.reporting.config;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderFind;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Returns true if there are {@link ClassificationModel} entries that match the given classification text.
 */
public class ClassificationExists extends GraphCondition
{
    private String filename;
    private String classificationPattern;

    private ClassificationExists(String classificationPattern)
    {
        this.classificationPattern = "[\\s\\S]*" + classificationPattern + "[\\s\\S]*";
    }

    /**
     * Specifies the regular expression to use when searching {@link ClassificationModel} entries.
     */
    public static ClassificationExists withClassification(String classificationPattern)
    {
        return new ClassificationExists(classificationPattern);
    }

    /**
     * Only consider entries that reference a file with the given filename.
     */
    public ClassificationExists in(String filename)
    {
        this.filename = filename;
        return this;
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        QueryBuilderFind q = Query.fromType(ClassificationModel.class);
        if (StringUtils.isNotBlank(filename))
        {
            q.piped(new QueryGremlinCriterion()
            {
                private static final String CLASSIFICATION_STEP = "classificationModel";

                @Override
                public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
                {
                    pipeline.as(CLASSIFICATION_STEP);
                    pipeline.out(ClassificationModel.FILE_MODEL);
                    pipeline.has(FileModel.FILE_NAME, filename);
                    pipeline.back(CLASSIFICATION_STEP);
                }
            });
        }
        q.withProperty(ClassificationModel.CLASSIFICATION, QueryPropertyComparisonType.REGEX, classificationPattern);
        return q.evaluate(event, context);
    }

}
