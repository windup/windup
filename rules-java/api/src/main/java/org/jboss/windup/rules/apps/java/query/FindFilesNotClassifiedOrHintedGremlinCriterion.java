package org.jboss.windup.rules.apps.java.query;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.forge.furnace.util.Sets;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.FileLocationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This provides a helper class that can be used execute a Gremlin search returning all FileModels that do not have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindFilesNotClassifiedOrHintedGremlinCriterion {
    public Iterable<Vertex> query(final GraphContext context, Iterable<Vertex> initialVertices) {
        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.total");

        final Set<Vertex> initialVerticesList = Sets.toSet(initialVertices);

        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(context.getGraph()).V(initialVertices);

        final Set<Vertex> allClassifiedOrHintedVertices = new HashSet<>();

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");
        // create a pipeline to get all hinted items
        new GraphTraversalSource(context.getGraph())
                .V(context.getQuery(InlineHintModel.class).getRawTraversal().toList())
                .as("fileLocation1")
                .out(FileLocationModel.FILE_MODEL)
                .filter(v -> initialVerticesList.contains(v.get()))
                .fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");
        // create a pipeline to get all items with attached classifications
        new GraphTraversalSource(context.getGraph())
                .V(context.getQuery(ClassificationModel.class).getRawTraversal().toList())
                .as("fileModel2").out(ClassificationModel.FILE_MODEL).filter(v -> initialVerticesList.contains(v.get()))
                .fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");

        pipeline.filter(it -> {
            Vertex v = it.get();
            FileModel f = context.getFramed().frameElement(v, FileModel.class);

            //1. we don't want to show files with hints/classifications
            if (allClassifiedOrHintedVertices.contains(v)) {
                return false;
            }

            //2. we don't want to show our generated classes in the report
            if (f.isWindupGenerated() != null && f.isWindupGenerated()) {
                return false;
            }

            //3. we don't want to show class in case it's .java decompiled file has hints/classifications
            if (f instanceof JavaClassFileModel) {
                Iterator<Vertex> decompiled = v.vertices(Direction.OUT, JavaClassFileModel.DECOMPILED_FILE);

                if (decompiled.hasNext()) {
                    JavaSourceFileModel source = context.getFramed().frameElement(decompiled.next(), JavaSourceFileModel.class);

                    if (allClassifiedOrHintedVertices.contains(source)) {
                        return false;
                    }
                }
            }

            return true;
        });

        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.total");
        return pipeline.toList();
    }
}
