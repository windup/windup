package org.jboss.windup.rules.apps.java.service;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaInlineHintModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceModel;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;

public class JavaInlineHintService extends GraphService<JavaInlineHintModel>
{

    @Inject
    public JavaInlineHintService(GraphContext context)
    {
        super(context, JavaInlineHintModel.class);
    }

    public Map<String, Integer> getPackageUseFrequencies(ProjectModel projectModel, int nameDepth, boolean recursive)
    {
        Map<String, Integer> packageUseCount = new HashMap<>();
        getPackageUseFrequencies(packageUseCount, projectModel, nameDepth, recursive);
        return packageUseCount;
    }

    private void getPackageUseFrequencies(Map<String, Integer> data, ProjectModel projectModel, int nameDepth,
                boolean recursive)
    {
        // 1. Get all JavaHints for the given project
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(projectModel.asVertex());
        pipeline.in(FileModel.FILE_TO_PROJECT_MODEL).in(InlineHintModel.FILE_MODEL);

        // 2. Organize them by package name
        // summarize results.
        for (Vertex javaInlineHintVertex : pipeline)
        {
            JavaInlineHintModel javaInlineHint = frame(javaInlineHintVertex);

            int val = 1;
            TypeReferenceModel typeReferenceModel = javaInlineHint.getTypeReferenceModel();
            if (typeReferenceModel == null)
            {
                continue;
            }

            String pattern = typeReferenceModel.getSourceSnippit();
            String[] keyArray = pattern.split("\\.");

            if (keyArray.length > 1 && nameDepth > 1)
            {
                StringBuilder patternSB = new StringBuilder();
                for (int i = 0; i < nameDepth; i++)
                {
                    if (patternSB.length() != 0)
                    {
                        patternSB.append(".");
                    }
                    patternSB.append(keyArray[i]);
                }
                patternSB.append(".*");
                pattern = patternSB.toString();
            }
            if (data.containsKey(pattern))
            {
                val = data.get(pattern);
                val++;
            }
            data.put(pattern, val);
        }

        if (recursive)
        {
            for (ProjectModel childProject : projectModel.getChildProjects())
            {
                getPackageUseFrequencies(data, childProject, nameDepth, recursive);
            }
        }
    }
}
