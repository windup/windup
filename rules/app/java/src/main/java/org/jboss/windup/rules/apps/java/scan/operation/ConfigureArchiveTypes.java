package org.jboss.windup.rules.apps.java.scan.operation;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveModelPointer;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.util.GraphUtil;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ConfigureArchiveTypes extends AbstractIterationOperation<ArchiveModel>
{
    // @Inject
    private Iterable<ArchiveModelPointer<? extends ArchiveModel>> archiveModelPointers;

    private HashMap<String, Class<? extends ArchiveModel>> suffixToModelClass = new HashMap<>();

    public ConfigureArchiveTypes(String variableName,
                Iterable<ArchiveModelPointer<? extends ArchiveModel>> archiveModelPointers)
    {
        super(ArchiveModel.class, variableName);
        this.archiveModelPointers = archiveModelPointers;
        initTypes();
    }

    public static ConfigureArchiveTypes forVar(String variableName,
                Iterable<ArchiveModelPointer<? extends ArchiveModel>> archiveModelPointers)
    {
        return new ConfigureArchiveTypes(variableName, archiveModelPointers);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel archiveModel)
    {
        GraphContext graphContext = event.getGraphContext();
        String filename = archiveModel.getArchiveName();
        WindupVertexFrame newFrame = null;

        for (Map.Entry<String, Class<? extends ArchiveModel>> entry : suffixToModelClass.entrySet())
        {
            if (StringUtils.endsWith(filename, entry.getKey()))
            {
                newFrame = GraphUtil.addTypeToModel(graphContext, archiveModel, entry.getValue());
            }
        }

        if (newFrame != null)
        {
            Iteration.setCurrentPayload(Variables.instance(event), getVariableName(), newFrame);
        }
    }

    private void initTypes()
    {
        for (ArchiveModelPointer<?> ptr : this.archiveModelPointers)
        {
            this.suffixToModelClass.put(ptr.getArchiveFileSuffix(), ptr.getModelClass());
        }
    }
}
