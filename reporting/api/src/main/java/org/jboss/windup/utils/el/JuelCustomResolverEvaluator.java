package org.jboss.windup.utils.el;

import java.lang.reflect.Method;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ListELResolver;
import javax.el.MapELResolver;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 * JUEL: http://juel.sourceforge.net/guide/start.html
 */
public class JuelCustomResolverEvaluator implements IExprLangEvaluator {
    private static final ExpressionFactory JUEL_FACTORY = new de.odysseus.el.ExpressionFactoryImpl();
    private final IVariablesProvider varProvider;


    public JuelCustomResolverEvaluator( IVariablesProvider variableProvider ) {
        this.varProvider = variableProvider;
    }


    // TODO: Move ELContext anonymous inner class to normal outer; don't create it for every call.
    @Override
    public String evaluateEL( String expr ) {
        if( expr == null )
            throw new IllegalArgumentException("The expression param is null.");
        
        // CompositeELResolver allows to resolve from multiple sources.
        final CompositeELResolver resolver = new CompositeELResolver();
        
        // Here I want to use Map to be able to add some values, e.g. from user input.
        resolver.add( new MapELResolver() );
        resolver.add( new ListELResolver() );
        
        // BeanELDefaultStringResolver is my implementation which returns "" if it can't find given variable
        // (instead of throwing an exception).
        resolver.add( new BeanELDefaultStringResolver("") );
        
        //de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
        ELContext elCtx = new ELContext() {
            @Override public ELResolver getELResolver() { return resolver; }

            @Override public FunctionMapper getFunctionMapper() { return THROW_MAPPER; }

            @Override public VariableMapper getVariableMapper() { 
                return new ProvidedVariableJuelMapper( JuelCustomResolverEvaluator.this.varProvider );
            }
        };
        
        try {
            ValueExpression valueExpr = JUEL_FACTORY.createValueExpression( elCtx, expr, String.class );
            return (String) valueExpr.getValue( elCtx );
        } catch( javax.el.PropertyNotFoundException ex ) {
            throw new IllegalArgumentException( "Can't eval '" + expr + "':\n    " + ex.getMessage(), ex );
        } catch( Throwable ex ) {
            throw new IllegalArgumentException( "Can't eval '" + expr + "':\n    " + ex.getMessage(), ex );
        }
    }
    
    
    /**
     *  Throws UnsupportedOperationException for any call.
     */
    static final FunctionMapper THROW_MAPPER = 
            new FunctionMapper() {
                @Override public Method resolveFunction( String prefix, String localName ) {
                    throw new UnsupportedOperationException( "No functions supported." );
                }
            };
    
}// class
