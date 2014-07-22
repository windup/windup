package org.jboss.windup.utils.el;


import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Simple EL evaluator which only works with a flat map.
 * 
 *  @author Ondrej Zizka, ozizka at redhat.com
 *  @deprecated 
 */
public class SimpleEvaluator implements IExprLangEvaluator {
    private static final Logger log = LoggerFactory.getLogger( SimpleEvaluator.class );
    private final Map<String, ? extends Object> properties;


    public SimpleEvaluator( Map<String, ? extends Object> properties ) {
        this.properties = properties;
    }


    @Override
    public String evaluateEL( String template ) {
        StringTokenizer st = new StringTokenizer( template );
        String text = st.nextToken( "${" );
        StringBuilder sb = new StringBuilder();
        // Parse the template: "Hello ${person.name} ${person.surname}, ${person.age}!"
        do {
            try {
                sb.append( text );
                if( !st.hasMoreTokens() ) {
                    break;
                }
                // "${foo.bar[a]"
                String expr = st.nextToken( "}" );
                // "foo.bar[a].baz"
                expr = expr.substring( 2 );
                // "foo"
                String var = StringUtils.substringBefore( expr, "." );
                Object subject = properties.get( var );
                // "bar[a].baz"
                String propPath = StringUtils.substringAfter( expr, "." );
                sb.append( resolveProperty2( subject, propPath ) );
                text = st.nextToken( "${" );
                text = text.substring( 1 );
            } catch( NoSuchElementException ex ) {
                // Unclosed ${
                log.warn( "Unclosed ${ expression, missing } : " + template );
            }
        } while( true );
        return sb.toString();
    }


    // Simple
    private String resolveProperty( Object subject, String propPath ) {
        if( subject == null ) {
            return "";
        }
        return subject.toString();
    }


    // BeanUtils
    private String resolveProperty2( Object subject, String propPath ) {
        if( subject == null ) {
            return "";
        }
        if( propPath == null || propPath.isEmpty() ) {
            return subject.toString();
        }
        try {
            return "" + PropertyUtils.getProperty( subject, propPath );
        } catch( IllegalAccessException | InvocationTargetException | NoSuchMethodException ex ) {
            log.warn( "Failed resolving '" + propPath + "' on " + subject + ":\n    " + ex.getMessage() );
            if( log.isTraceEnabled() ) {
                log.trace( "    Stacktrace:\n", ex );
            }
            return "";
        }
    }

} // class
