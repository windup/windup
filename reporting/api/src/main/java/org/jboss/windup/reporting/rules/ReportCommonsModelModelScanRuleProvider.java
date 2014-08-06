package org.jboss.windup.reporting.rules;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.frames.FramedGraph;
import java.util.logging.Logger;
import javax.inject.Inject;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.model.ModelModel;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ReportCommonsModelModel;
import org.jboss.windup.util.exception.WindupException;
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
    private static final Logger log = Logger.getLogger(ReportCommonsModelModelScanRuleProvider.class.getName());

    
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
                
                // For each ModelModel...
                Iteration.over("models").as("model").perform(
                    new AbstractIterationOperation(null, null) {
                        public void perform( GraphRewrite event, EvaluationContext context, WindupVertexFrame payload ) {
                            ModelModel model = varstack.findSingletonVariable(ModelModel.class, "model");
                            // ...extend the type and add meta information found in the class.
                            ReportCommonsModelModel reportModel = GraphService.addTypeToModel( graph, model, ReportCommonsModelModel.class);
                            try {
                                Class<?> modelClass = ReportCommonsModelModelScanRuleProvider.class.getClassLoader().loadClass(model.getClassName());
                                if( ! WindupVertexFrame.class.isAssignableFrom( modelClass ))
                                    throw new WindupException("Model class is not a " + WindupVertexFrame.class.getSimpleName() + "!");
                                ReportsCommonsModelModelUtil.fill(reportModel, (Class<? extends WindupVertexFrame>) modelClass);
                            }
                            catch(ClassNotFoundException ex){
                                throw new WindupException("Failed loading model class: " + model.getClassName());
                            }
                            log.info("Storing ReportCommons for: " + model.getClassName());
                        }
                    }
                );
            }
        });
    }

    private static Class<?> getClass( String className ) throws ClassNotFoundException {
        return Class.forName(className);
    }

}// class
