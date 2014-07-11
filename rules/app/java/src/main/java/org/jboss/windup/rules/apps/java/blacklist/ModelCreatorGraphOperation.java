package org.jboss.windup.rules.apps.java.blacklist;

import java.util.List;

import javax.inject.Inject;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.ext.java.events.JavaASTEventService;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Gathers the {@link ASTEventEvaluator} classes and after the perform is called, register them in the {@link JavaASTEventService} service.
 * @author mbriskar
 *
 */
public class ModelCreatorGraphOperation extends GraphOperation
{

    @Inject
    JavaASTEventService eventService;
    
    private List<ASTEventEvaluator> interests;
    
    public ModelCreatorGraphOperation add(ASTEventEvaluator interest) {
        interests.add(interest);
        return this;
    }
    
    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        for(ASTEventEvaluator interest: interests) {
            eventService.registerInterest(interest);
        }
    }
    
    public static ModelCreatorGraphOperation create() {
        return new ModelCreatorGraphOperation();
    }

}
