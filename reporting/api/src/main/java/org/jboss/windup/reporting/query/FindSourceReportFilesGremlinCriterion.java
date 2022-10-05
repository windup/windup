package org.jboss.windup.reporting.query;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Scope;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.query.QueryGremlinCriterion;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This provides a helper class that can be used in a Windup Query call to execute a Gremlin search returning all FileModels that have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 */
public class FindSourceReportFilesGremlinCriterion implements QueryGremlinCriterion {
    @SuppressWarnings("unchecked")
    @Override
    public void query(GraphRewrite event, GraphTraversal<?, Vertex> pipeline) {
        GraphContext context = event.getGraphContext();

        Set all = new HashSet<>();

        // create a pipeline to get all hinted items
        GraphTraversal<Vertex, Vertex> hintPipeline = new GraphTraversalSource(event.getGraphContext().getGraph())
                .V(context.getQuery(FileModel.class).getRawTraversal().toList());
        hintPipeline.as("fileModel1").in(FileLocationModel.FILE_MODEL)
                .has(WindupVertexFrame.TYPE_PROP, P.eq(InlineHintModel.TYPE)).select("fileModel1").fill(all);

        // create a pipeline to get all items with attached classifications
        GraphTraversal<Vertex, Vertex> classificationPipeline = new GraphTraversalSource(event.getGraphContext().getGraph())
                .V(context.getQuery(FileModel.class).getRawTraversal().toList());
        classificationPipeline.as("fileModel2").in(ClassificationModel.FILE_MODEL)
                .has(WindupVertexFrame.TYPE_PROP, P.eq(ClassificationModel.TYPE))
                .select("fileModel2")
                .fill(all);

        // create a pipeline to get all items with attached technology tags
        GraphTraversal<Vertex, Vertex> technologyTagPipeline = new GraphTraversalSource(event.getGraphContext().getGraph())
                .V(context.getQuery(FileModel.class).getRawTraversal().toList());
        technologyTagPipeline.as("fileModel3").in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL)
                .has(WindupVertexFrame.TYPE_PROP, P.eq(TechnologyTagModel.TYPE))
                .has(TechnologyTagModel.LEVEL, TechnologyTagLevel.IMPORTANT.toString())
                .select("fileModel3")
                .fill(all);

        // Also return SourceFileModel results with the generate source flag set to true
        GraphTraversal<Vertex, Vertex> generateSourceReportPropertyPipeline = new GraphTraversalSource(event.getGraphContext().getGraph())
                .V(context.getQuery(FileModel.class).getRawTraversal().toList());
        generateSourceReportPropertyPipeline
                .has(SourceFileModel.GENERATE_SOURCE_REPORT, true)
                .fill(all);


        // combine these to get all file models that have either classifications or blacklists
        pipeline.filter((Predicate<Traverser<Vertex>>) vertexTraverser -> all.contains(vertexTraverser.get()));
    }
}
