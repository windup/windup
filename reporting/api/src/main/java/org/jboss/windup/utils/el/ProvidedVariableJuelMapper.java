package org.jboss.windup.utils.el;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 *  Delegates the variable resolving to an IVariablesProvider provided to the constructor.
 *  Read-only.

 *  @author Ondrej Zizka, ozizka at redhat.com
 */
public class ProvidedVariableJuelMapper extends javax.el.VariableMapper {
    private static final ExpressionFactory JUEL_FACTORY = new de.odysseus.el.ExpressionFactoryImpl();
    

    private final IExprLangEvaluator.IVariablesProvider<Object> provider;

    
    public ProvidedVariableJuelMapper( IExprLangEvaluator.IVariablesProvider<Object> provider ) {
        this.provider = provider;
    }
    

    @Override public ValueExpression resolveVariable( String varName ) {
        final Object var = this.provider.getVariable( varName );
        return JUEL_FACTORY.createValueExpression( var, Object.class );
    }
    
    @Override public ValueExpression setVariable( String variable, ValueExpression expression ) {
        throw new UnsupportedOperationException( "Read-only, can't set: " + variable );
    }
    
}// class