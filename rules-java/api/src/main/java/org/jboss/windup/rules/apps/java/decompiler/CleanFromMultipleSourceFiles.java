package org.jboss.windup.rules.apps.java.decompiler;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Removes all the duplicates of .java file within a projectModel. There may be multiple especially in cases where .class is bundled with .java files.
 * Non-decompiled .java files have higher priority of being deleted than decompiled.
 */
public class CleanFromMultipleSourceFiles extends GraphOperation
{
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(
                    event.getGraphContext().getQuery().type(JavaSourceFileModel.class).vertices());
        final GraphContext gContext = event.getGraphContext();
        pipeline.groupBy(groupByProjectModelFunction(gContext), valueAsFramedVertex(gContext), returnVerticesToDelete(gContext)).cap();
        HashMap<?, List<JavaSourceFileModel>> m = (HashMap<?, List<JavaSourceFileModel>>) pipeline.next();
        for (List<JavaSourceFileModel> toBeDeleted : m.values())
        {
            for (JavaSourceFileModel javaSourceFileModel : toBeDeleted)
            {
                gContext.getGraph().removeVertex(javaSourceFileModel.asVertex());
            }

        }

    }

    // helping methods

    private PipeFunction groupByProjectModelFunction(final GraphContext context)
    {
        return new PipeFunction<Vertex, String>()
        {
            @Override public String compute(Vertex vertex)
            {
                JavaSourceFileModel javaModel = context.getFramed().frame(vertex, JavaSourceFileModel.class);
                // String that identifies 3 properties - projectModel + packageName + className that must be the same for vertices
                return javaModel.getProjectModel().getRootFileModel().getFilePath() + javaModel.getPackageName() + javaModel.getFileName();
            }
        };
    }

    private PipeFunction valueAsFramedVertex(final GraphContext context)
    {
        return new PipeFunction<Vertex, JavaSourceFileModel>()
        {
            @Override public JavaSourceFileModel compute(Vertex vertex)
            {
                return context.getFramed().frame(vertex, JavaSourceFileModel.class);
            }
        };
    }

    private PipeFunction returnVerticesToDelete(final GraphContext context)
    {
        return new PipeFunction<Collection<JavaSourceFileModel>, List<JavaSourceFileModel>>()
        {
            @Override public List<JavaSourceFileModel> compute(Collection<JavaSourceFileModel> javaClassFileModels)
            {
                boolean uniqueClassFound = false;
                List<JavaSourceFileModel> verticesToBeDeleted = new ArrayList<>();
                if (javaClassFileModels.size() == 0)
                {
                    return null;
                }
                Iterator<JavaSourceFileModel> iterator = javaClassFileModels.iterator();
                while (iterator.hasNext())
                {
                    JavaSourceFileModel javaModel = iterator.next();
                    if (javaModel.isDecompiled() && !uniqueClassFound)
                    {
                        uniqueClassFound = true;
                    }
                    else if (!iterator.hasNext() && !uniqueClassFound)
                    {
                        uniqueClassFound = true;
                    }
                    else
                    {
                        verticesToBeDeleted.add(javaModel);
                    }
                }

                return verticesToBeDeleted;
            }

        };
    }
}