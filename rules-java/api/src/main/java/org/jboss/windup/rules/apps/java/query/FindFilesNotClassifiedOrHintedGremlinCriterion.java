package org.jboss.windup.rules.apps.java.query;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import org.jboss.forge.furnace.util.Lists;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.ExecutionStatistics;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * This provides a helper class that can be used execute a Gremlin search returning all FileModels that do not have associated
 * {@link FileLocationModel}s or @{link ClassificationModel}s.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class FindFilesNotClassifiedOrHintedGremlinCriterion
{
    public Iterable<Vertex> query(final GraphContext context, Iterable<Vertex> initialVertices)
    {
        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.total");

        final List<Vertex> initialVerticesList = Lists.toList(initialVertices);

        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(initialVertices);

        final Set<Vertex> allClassifiedOrHintedVertices = new HashSet<>();

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");
        // create a pipeline to get all hinted items
        GremlinPipeline<Vertex, Vertex> hintPipeline = new GremlinPipeline<>(
                    context.getQuery().type(InlineHintModel.class).vertices());
        hintPipeline.as("fileLocation1").out(FileLocationModel.FILE_MODEL).retain(initialVerticesList);
        hintPipeline.fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.hintPipeline");

        ExecutionStatistics.get().begin("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");
        // create a pipeline to get all items with attached classifications
        GremlinPipeline<Vertex, Vertex> classificationPipeline = new GremlinPipeline<>(
                    context.getQuery().type(ClassificationModel.class).vertices());
        classificationPipeline.as("fileModel2").out(ClassificationModel.FILE_MODEL).retain(initialVerticesList);
        classificationPipeline.fill(allClassifiedOrHintedVertices);
        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.classificationPipeline");

        pipeline.filter(new PipeFunction<Vertex, Boolean>()
        {
            @Override
            public Boolean compute(Vertex v)
            {
                FileModel f = context.getFramed().frame(v, FileModel.class);

                //1. we don't want to show files with hints/classifications
                if (allClassifiedOrHintedVertices.contains(v))
                {
                    return false;
                }

                //2. we don't want to show our decompiled classes in the report
                if (f.isWindupGenerated())
                {
                    return false;
                }

                //3. we don't want to show class in case it's .java decompiled file has hints/classifications
                if (f instanceof JavaClassFileModel)
                {
                    Iterator<Vertex> decompiled = v.getVertices(Direction.OUT, JavaClassFileModel.DECOMPILED_FILE).iterator();
                    if (decompiled.hasNext())
                    {
                        JavaSourceFileModel source = context.getFramed().frame(decompiled.next(), JavaSourceFileModel.class);
                        if (allClassifiedOrHintedVertices.contains(source.asVertex()))
                        {
                            return false;
                        }
                    }
                }
                return true;
            }
        });

        ExecutionStatistics.get().end("FindFilesNotClassifiedOrHintedGremlinCriterion.total");
        return pipeline;
    }
}
