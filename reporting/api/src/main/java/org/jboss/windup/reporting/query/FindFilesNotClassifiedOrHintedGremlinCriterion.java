package org.jboss.windup.reporting.query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jboss.forge.furnace.util.Lists;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.ExecutionStatistics;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

/**
 * This provides a helper class that can be used execute a Gremlin search returning all FileModels that do not have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class FindFilesNotClassifiedOrHintedGremlinCriterion
{
    public Iterable<Vertex> query(GraphContext context, Iterable<Vertex> initialVertices)
    {
        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.total");

        final List<Vertex> initialVerticesList = Lists.toList(initialVertices);

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(initialVertices);

        final Set<Vertex> allClassifiedOrHintedVertices = new HashSet<>();

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");
        // create a pipeline to get all hinted items
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(InlineHintModel.class).vertices());
        hintPipeline.as("fileLocation1").out(FileLocationModel.FILE_MODEL).retain(initialVerticesList);
        hintPipeline.fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");
        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(ClassificationModel.class).vertices());
        classificationPipeline.as("fileModel2").out(ClassificationModel.FILE_MODEL).retain(initialVerticesList);
        classificationPipeline.fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");

        pipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex argument)
            {
                return !allClassifiedOrHintedVertices.contains(argument);
            }
        });

        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.total");
        return pipeline;
    }
}
