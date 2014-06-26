package org.jboss.windup.config.operation.foreach;


import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngineFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jboss.windup.config.exception.IllegalTypeArgumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gremlin Groovy based query.
 * TODO: Move to Graph Impl when tested.
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GremlinQuerier {
    private static final Logger log = LoggerFactory.getLogger( GremlinQuerier.class );
    
    private final Graph graph;


    public GremlinQuerier( Graph graph ) {
        this.graph = graph;
    }
    
    /**
     * Performs a Gremlin Groovy based query.
     * Pre-defined params are:
     *   <li> "g" for graph given in constructor.
     */
    public Iterable query( String query ) throws ScriptException{
        return this.query( query, null );
    }
    
    /**
     * Performs a Gremlin Groovy based query.
     * Allows to set the bindings - params will be available 
     * as variables in the Gremlin Groovy script.
     */
    public Iterable query( String query, Map<String,Object> params ) throws ScriptException{
        ScriptEngineManager manager = new ScriptEngineManager();
        //ScriptEngine engine = manager.getEngineByName("gremlin-groovy");
        ScriptEngine engine = new GremlinGroovyScriptEngineFactory().getScriptEngine();
        
        List results = new LinkedList();
        Bindings bindings = engine.createBindings();
        bindings.put("g", graph);
        bindings.put("results", results);
        
        if( params != null )
            bindings.putAll( params );
        
        //engine.eval("v.out('knows').has('name',name).fill(results)", bindings);
        engine.eval( query + ".fill(results)", bindings );
        
        return results;
    }

    /**
     * Performs a Gremlin Groovy based query.
     */
    public <T> Iterable<T> query( String query, Map<String,Object> params, Class<T> type ) throws ScriptException{
        
        Iterable results = this.query( query, params );
        
        // Check the types.
        if( type != null ){
            for( Object object : results ) {
                if( ! type.isAssignableFrom( object.getClass() ) )
                    throw new IllegalTypeArgumentException(query, type, object.getClass() );
            }
        }
        
        return results;
    }


}// class
