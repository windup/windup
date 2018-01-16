package org.jboss.windup.rules.apps.java.decompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;

/**
 * Removes all the duplicates of .java file within a projectModel. There may be multiple especially in cases where .class is bundled with .java files.
 * Non-decompiled .java files have higher priority of being deleted than decompiled.
 */
public class CleanFromMultipleSourceFiles extends GraphOperation
{
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        final GraphContext graphContext = event.getGraphContext();
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(graphContext.getGraph())
                .V(graphContext.getQuery(JavaSourceFileModel.class).getRawTraversal());
        List<Object> javaSourceFileModelsObjects = pipeline.group()
                .by(v -> {
                    return groupByProjectModelFunction(graphContext, (Vertex)v);
                })
                .map(traverser -> {
                    Map<Object, Object> map = (Map<Object, Object>) traverser.get();
                    List<JavaSourceFileModel> result = new ArrayList<>();
                    for (Map.Entry<Object, Object> entry : map.entrySet())
                    {
                        List<Vertex> vertices = (List<Vertex>)entry.getValue();
                        for (Vertex vertex : vertices) {
                            JavaSourceFileModel framed = graphContext.getFramed().frameElement(vertex, JavaSourceFileModel.class);
                            result.add(framed);
                        }
                    }
                    return result;
                })
                .unfold()
                .toList();

        List<JavaSourceFileModel> javaSourceFileModels = javaSourceFileModelsObjects.stream().map(o -> (JavaSourceFileModel)o).collect(Collectors.toList());
        returnVerticesToDelete(javaSourceFileModels)
                .forEach(javaSourceFileModel -> javaSourceFileModel.remove());
    }

    // helping methods

    private String groupByProjectModelFunction(final GraphContext context, final Vertex vertex)
    {
        JavaSourceFileModel javaModel = context.getFramed().frameElement(vertex, JavaSourceFileModel.class);
        // String that identifies 3 properties - projectModel + packageName + className that must be the same for vertices
        ProjectModel projectModel = javaModel.getProjectModel();
        String projectModelID = projectModel == null ? "" : projectModel.getId().toString();
        String packageName = javaModel.getPackageName() == null ? "" : javaModel.getPackageName();

        return projectModelID + "_" + packageName + "_" + javaModel.getFileName();
    }

    private List<JavaSourceFileModel> returnVerticesToDelete(final Collection<JavaSourceFileModel> javaSourceFileModels)
    {
        boolean uniqueClassFound = false;
        List<JavaSourceFileModel> verticesToBeDeleted = new ArrayList<>();
        if (javaSourceFileModels.isEmpty())
        {
            return Collections.emptyList();
        }
        Iterator<JavaSourceFileModel> iterator = javaSourceFileModels.iterator();
        while (iterator.hasNext())
        {
            JavaSourceFileModel javaModel = iterator.next();
            if (javaModel.isWindupGenerated() != null && javaModel.isWindupGenerated() && !uniqueClassFound)
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
}
