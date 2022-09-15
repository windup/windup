package org.jboss.windup.rules.apps.java.decompiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Removes all the duplicates of .java file within a projectModel. There may be multiple especially in cases where .class is bundled with .java files.
 * Non-decompiled .java files have higher priority of being deleted than decompiled.
 */
public class CleanFromMultipleSourceFiles extends GraphOperation {
    @Override
    public void perform(GraphRewrite event, EvaluationContext context) {
        final GraphContext graphContext = event.getGraphContext();

        List<Map<Object, Object>> javaSourceGroups = graphContext.getQuery(JavaSourceFileModel.class).getRawTraversal()
                .group()
                .by(v -> groupByProjectModelFunction(graphContext, (Vertex) v))
                .toList();

        final GraphService<JavaSourceFileModel> service = new GraphService<>(event.getGraphContext(), JavaSourceFileModel.class);
        for (Map<Object, Object> duplicateLists : javaSourceGroups) {
            for (Object duplicateListObject : duplicateLists.values()) {
                List<Vertex> duplicateList = (List<Vertex>) duplicateListObject;
                List<JavaSourceFileModel> toDelete = returnVerticesToDelete(service, duplicateList);
                toDelete.forEach(javaSourceFileModel -> javaSourceFileModel.remove());
            }
        }
    }

    // helping methods

    private String groupByProjectModelFunction(final GraphContext context, final Vertex vertex) {
        JavaSourceFileModel javaModel = context.getFramed().frameElement(vertex, JavaSourceFileModel.class);
        // String that identifies 3 properties - projectModel + packageName + className that must be the same for vertices
        ProjectModel projectModel = javaModel.getProjectModel();
        String projectModelID = projectModel == null ? "" : projectModel.getId().toString();
        String packageName = javaModel.getPackageName() == null ? "" : javaModel.getPackageName();

        return projectModelID + "_" + packageName + "_" + javaModel.getFileName();
    }

    private List<JavaSourceFileModel> returnVerticesToDelete(final GraphService<JavaSourceFileModel> service, final Collection<Vertex> javaSourceVertices) {
        boolean uniqueClassFound = false;
        List<JavaSourceFileModel> verticesToBeDeleted = new ArrayList<>();
        if (javaSourceVertices.isEmpty()) {
            return Collections.emptyList();
        }
        Iterator<Vertex> iterator = javaSourceVertices.iterator();
        while (iterator.hasNext()) {
            Vertex v = iterator.next();
            JavaSourceFileModel javaModel = service.frame(v);
            if (javaModel.isWindupGenerated() != null && javaModel.isWindupGenerated() && !uniqueClassFound) {
                uniqueClassFound = true;
            } else if (!iterator.hasNext() && !uniqueClassFound) {
                uniqueClassFound = true;
            } else {
                verticesToBeDeleted.add(javaModel);
            }
        }

        return verticesToBeDeleted;
    }
}
