package org.jboss.windup.config.rules;


import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.frames.FramedGraph;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.config.model.ModelModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Adds the discovered models to the graph.
 * This has to be in Config.Impl as it uses .config. classes.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ModelScanRuleProvider extends WindupRuleProvider
{
    @Inject
    private GraphTypeManager graphTypeManager;    
    
    @Inject 
    private GraphContext graph;
    
    
    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public void enhanceMetadata(Context context)
    {
        context.put(RuleMetadata.CATEGORY, "core");
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        
        return ConfigurationBuilder.begin().addRule()
        .perform( new GraphOperation() {
            @Override
            public void perform( GraphRewrite event, EvaluationContext context ) {
                
                Set<Class<? extends WindupVertexFrame>> types
                        = graphTypeManager.getRegisteredTypes();
                
                FramedGraph<TitanGraph> framed = event.getGraphContext().getFramed();
                for( Class<? extends WindupVertexFrame> cls : types ) {
                    ModelModel model = framed.addVertex( null, ModelModel.class );
                    model.setClassName( cls.getName() );
                    System.out.println( "Storing ModelModel for: " + cls.getName() );
                }
            }
        });
    }

    

}// class
