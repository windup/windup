package org.jboss.windup.reporting.query;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.rules.files.model.FileLocationModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * This provides a helper class that can be used in a Windup Query call to execute a Gremlin search returning all FileModels that have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 */
public class FindSourceReportFilesGremlinCriterion implements QueryGremlinCriterion
{
    @SuppressWarnings("unchecked")
    @Override
    public void query(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipeline)
    {
        GraphContext context = event.getGraphContext();

        // create a pipeline to get all blacklisted items
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());
        hintPipeline.as("fileModel1").in(FileLocationModel.FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, InlineHintModel.TYPE).back("fileModel1");

        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());
        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, ClassificationModel.TYPE)
                    .back("fileModel2");

        // create a pipeline to get all items with attached technology tags
        GremlinPipeline<Vertex, Vertex> technologyTagPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(FileModel.class).vertices());
        technologyTagPipeline.as("fileModel3").in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE)
                    .has(TechnologyTagModel.LEVEL, TechnologyTagLevel.IMPORTANT.toString())
                    .back("fileModel3");

        // Also return SourceFileModel results with the generate source flag set to true
        GremlinPipeline<Vertex, Vertex> generateSourceReportPropertyPipeline = new GremlinPipeline<Vertex, Vertex>(
                    context.getQuery().type(SourceFileModel.class).vertices());
        generateSourceReportPropertyPipeline
                    .has(SourceFileModel.GENERATE_SOURCE_REPORT, true);

        // combine these to get all file models that have either classifications or blacklists
        pipeline.or(hintPipeline, classificationPipeline, technologyTagPipeline, generateSourceReportPropertyPipeline);
    }
}
