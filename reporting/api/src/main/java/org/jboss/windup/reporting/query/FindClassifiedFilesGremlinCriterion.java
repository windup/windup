package org.jboss.windup.reporting.query;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileLocationModel;
import org.jboss.windup.reporting.model.InlineHintModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This provides a helper class that can be used in a Windup Query call to execute a Gremlin search returning all
 * FileModels that have associated blacklists or classifications.
 * 
 */
public class FindClassifiedFilesGremlinCriterion implements QueryGremlinCriterion
{
    @SuppressWarnings("unchecked")
    @Override
    public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
    {
        GraphContext context = event.getGraphContext();

        // create a pipeline to get all blacklisted items
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());
        hintPipeline.as("fileModel1").in(FileLocationModel.FILE_MODEL).back("fileModel1");

        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());

        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                    .back("fileModel2");

        // combine these to get all file models that have either classifications or blacklists
        pipeline.or(hintPipeline, classificationPipeline);
    }
}
