package org.jboss.windup.reporting.rules;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.frames.FramedGraph;
import javax.inject.Inject;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.config.model.ModelModel;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportCommonsModelModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.Context;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Queries for the existing models, extracts their metadata and caches them to the graph.
 * Needed for the reporting to reach these data over standard layers 
 * instead of using reflection every time.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class ReportCommonsModelModelScanRuleProvider extends WindupRuleProvider
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
        context.put(RuleMetadata.CATEGORY, "report");
    }

    @Override
    public Configuration getConfiguration(GraphContext gCtx)
    {
        
        return ConfigurationBuilder.begin().addRule()
        .when(
            Query.find(ModelModel.class).as("models")
        )
        .perform( new GraphOperation() {
            public void perform( GraphRewrite event, EvaluationContext eCtx ) {
                
                final FramedGraph<TitanGraph> framed = event.getGraphContext().getFramed();
                final Variables varstack = Variables.instance(event);
                
                // For each model,
                Iteration.over("models").as("model").perform(
                    new AbstractIterationOperation(null, null ) {
                        public void perform( GraphRewrite event, EvaluationContext context, WindupVertexFrame payload ) {
                            ModelModel model = varstack.findSingletonVariable(ModelModel.class, "model");
                            ReportCommonsModelModel reportModel = GraphService.addTypeToModel( graph, model, ReportCommonsModelModel.class);
                            Class<?> modelClass = ReportCommonsModelModelScanRuleProvider.getClass(model.getClassName());
                            ReportsCommonsModelModelUtil.fill(reportModel, modelClass);
                        }
                    }
                );
                    System.out.println( "Storing ReportCommonsModelModel for: " + mo );
                }
            }
        });
    }

    private static Class<?> getClass( String className ) {
        return Class.forName(className);
    }

}// class
