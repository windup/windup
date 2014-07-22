package org.jboss.windup.utils.el;

import java.util.Map;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 * JUEL: http://juel.sourceforge.net/guide/start.html
 */
public class JuelSimpleEvaluator implements IExprLangEvaluator {
    private static final ExpressionFactory JUEL_FACTORY = new de.odysseus.el.ExpressionFactoryImpl();
    private final Map<String, ? extends Object> properties;


    public JuelSimpleEvaluator( Map<String, ? extends Object> properties ) {
        this.properties = properties;
    }


    public String evaluateEL( String expr ) {
        // Pre-fill a context with values.
        de.odysseus.el.util.SimpleContext context = new de.odysseus.el.util.SimpleContext();
        for( Map.Entry<String, ? extends Object> entry : properties.entrySet() ) {
            context.setVariable( entry.getKey(), JUEL_FACTORY.createValueExpression( entry.getValue(), String.class ) );
        }
        // Create the value expression and evaluate.
        ValueExpression valueExpr = JUEL_FACTORY.createValueExpression( context, expr, String.class );
        return (String) valueExpr.getValue( context );
    }

}// class
