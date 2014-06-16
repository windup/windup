package org.jboss.windup.config.operation.ruleelement;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.services.Imported;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.selectables.SelectionFactory;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ArchiveModelPointer;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ConfigureArchiveTypes extends AbstractIterationOperator<ArchiveModel>
{
    private @Inject Imported<ArchiveModelPointer> archiveModelPointers;
    
    private HashMap<String, Class> suffixToModelClass;
    
    
    public ConfigureArchiveTypes(String variableName)
    {
        super(ArchiveModel.class, variableName);
        
        initTypes();
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
        
        for( Map.Entry<String, Class> entry : suffixToModelClass.entrySet() ) {
            if( StringUtils.endsWith( filename, entry.getKey() ) ){
                newFrame = GraphUtil.addTypeToModel(graphContext, archiveModel, entry.getValue());
            }
        }
        
        /*
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
        }/**/
        
        if (newFrame != null)
        {
            SelectionFactory.instance(event).setCurrentPayload(getVariableName(), newFrame);
        }
    }


    private void initTypes() {
        for( ArchiveModelPointer ptr : this.archiveModelPointers ) {
            this.suffixToModelClass.put( ptr.getArchiveFileSuffix(), ptr.getModelClass() );
        }
    }
}
