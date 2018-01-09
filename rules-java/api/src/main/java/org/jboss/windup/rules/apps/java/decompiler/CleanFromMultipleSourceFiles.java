package org.jboss.windup.rules.apps.java.decompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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
        final GraphContext gContext = event.getGraphContext();
        GraphTraversal<Vertex, Vertex> pipeline = new GraphTraversalSource(gContext.getGraph())
                .V(gContext.getQuery(JavaSourceFileModel.class).getRawTraversal());
        List<JavaSourceFileModel> javaClassFileModels = pipeline.group()
                .by(v -> groupByProjectModelFunction(gContext, (Vertex)v))
                .map(v -> gContext.getFramed().frameElement((Vertex)v, JavaSourceFileModel.class))
                .toList();
        returnVerticesToDelete(javaClassFileModels)
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

    private List<JavaSourceFileModel> returnVerticesToDelete(final Collection<JavaSourceFileModel> javaClassFileModels)
    {
        boolean uniqueClassFound = false;
        List<JavaSourceFileModel> verticesToBeDeleted = new ArrayList<>();
        if (javaClassFileModels.isEmpty())
        {
            return null;
        }
        Iterator<JavaSourceFileModel> iterator = javaClassFileModels.iterator();
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
