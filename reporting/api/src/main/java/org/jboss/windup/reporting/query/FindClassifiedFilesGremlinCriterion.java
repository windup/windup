package org.jboss.windup.reporting.query;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.ClassificationModel;

import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.frames.FramedGraph;
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
        FramedGraph<EventGraph<TitanGraph>> framed = event.getGraphContext().getFramed();

        // create a pipeline to get all items with inline hints
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<Vertex, Vertex>(
                    framed
                                .query()
                                .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, FileModel.TYPE)
                                .vertices());
        hintPipeline.as("fileModel1").in(InlineHintModel.FILE_MODEL).back("fileModel1");

        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                    framed
                                .query()
                                .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, FileModel.TYPE)
                                .vertices());
        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                    .back("fileModel2");

        // combine these to get all file models that have either classifications or hints
        pipeline.or(hintPipeline, classificationPipeline);
    }
}
