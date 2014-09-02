package org.jboss.windup.reporting.query;

import java.util.HashSet;
import java.util.Set;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.FileLocationModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

/**
 * This provides a helper class that can be used execute a Gremlin search returning all FileModels that do not have
 * associated {@link FileLocationModel}s or @{link ClassificationModel}s.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class FindFilesNotClassifiedOrHintedGremlinCriterion
{
    @SuppressWarnings("unchecked")
    public Iterable<Vertex> query(GraphContext context, Iterable<Vertex> initialVertices)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(initialVertices);

        final Set<Vertex> allClassifiedOrHintedVertices = new HashSet<>();

        // create a pipeline to get all blacklisted items
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());
        hintPipeline.as("fileModel1").in(FileLocationModel.FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, FileLocationModel.TYPE)
                    .back("fileModel1");
        hintPipeline.fill(allClassifiedOrHintedVertices);

        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());

        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE)
                    .back("fileModel2");
        classificationPipeline.fill(allClassifiedOrHintedVertices);

        pipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex argument)
            {
                return !allClassifiedOrHintedVertices.contains(argument);
            }
        });

        return pipeline;
    }
}
