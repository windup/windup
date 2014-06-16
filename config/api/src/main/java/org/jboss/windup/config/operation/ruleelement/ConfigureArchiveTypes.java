package org.jboss.windup.config.operation.ruleelement;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.rules.apps.ejb.model.EarArchiveModel;
import org.jboss.windup.rules.apps.java.scan.model.JarArchiveModel;
import org.jboss.windup.rules.apps.ejb.model.WarArchiveModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ConfigureArchiveTypes extends AbstractIterationOperator<ArchiveModel>
{
    public ConfigureArchiveTypes(String variableName)
    {
        super(ArchiveModel.class, variableName);
    }

    public static ConfigureArchiveTypes forVar(String variableName)
    {
        return new ConfigureArchiveTypes(variableName);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel archiveModel)
    {
        GraphContext graphContext = event.getGraphContext();
        String filename = archiveModel.getArchiveName();
        WindupVertexFrame newFrame = null;
        if (StringUtils.endsWith(filename, ".jar"))
        {
            newFrame = GraphUtil.addTypeToModel(graphContext, archiveModel, JarArchiveModel.class);
        }
        else if (StringUtils.endsWith(filename, ".war"))
        {
            newFrame = GraphUtil.addTypeToModel(graphContext, archiveModel, WarArchiveModel.class);
        }
        else if (StringUtils.endsWith(filename, ".ear"))
        {
            newFrame = GraphUtil.addTypeToModel(graphContext, archiveModel, EarArchiveModel.class);
        }
        if (newFrame != null)
        {
            SelectionFactory.instance(event).setCurrentPayload(getVariableName(), newFrame);
        }
    }
}
