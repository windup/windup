package org.jboss.windup.rules.apps.java.scan.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveType;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Adds the vertex types to ArchiveModels as per the file's extension.
 */
public class ConfigureArchiveTypes extends AbstractIterationOperation<ArchiveModel> {

    private GraphTypeManager graphTypeManager;

    private HashMap<String, Class<? extends WindupVertexFrame>> suffixToModelClass = new HashMap<>();

    public ConfigureArchiveTypes(String variableName, GraphTypeManager graphTypeManager) {
        super(variableName);
        this.graphTypeManager = graphTypeManager;
        initTypes();
    }

    public ConfigureArchiveTypes(GraphTypeManager graphTypeManager) {
        super();
        this.graphTypeManager = graphTypeManager;
        initTypes();
    }

    public static ConfigureArchiveTypes forVar(String variableName, GraphTypeManager graphTypeManager) {
        return new ConfigureArchiveTypes(variableName, graphTypeManager);
    }

    public static ConfigureArchiveTypes withTypeManager(GraphTypeManager graphTypeManager) {
        return new ConfigureArchiveTypes(graphTypeManager);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel archiveModel) {
        GraphContext graphContext = event.getGraphContext();
        String filename = archiveModel.getArchiveName();
        WindupVertexFrame newFrame = null;

        for (Map.Entry<String, Class<? extends WindupVertexFrame>> entry : suffixToModelClass.entrySet()) {
            if (StringUtils.endsWith(filename, entry.getKey())) {
                newFrame = GraphService.addTypeToModel(graphContext, archiveModel, entry.getValue());
            }
        }

        if (newFrame != null) {
            Iteration.setCurrentPayload(Variables.instance(event), getVariableName(), newFrame);
        }
    }

    private void initTypes() {
        Set<Class<? extends WindupFrame<?>>> frameClasses = graphTypeManager.getRegisteredTypes();
        for (Class<? extends WindupFrame<?>> frameClass : frameClasses) {
            // only use vertex frames for this mapping
            if (!WindupVertexFrame.class.isAssignableFrom(frameClass))
                continue;

            @SuppressWarnings("unchecked")
            Class<? extends WindupVertexFrame> vertexFrame = (Class<? extends WindupVertexFrame>) frameClass;

            ArchiveType archiveType = frameClass.getAnnotation(ArchiveType.class);
            if (archiveType != null) {
                this.suffixToModelClass.put(archiveType.value(), vertexFrame);
            }
        }
    }

    @Override
    public String toString() {
        return "ConfigureArchiveTypes";
    }
}
