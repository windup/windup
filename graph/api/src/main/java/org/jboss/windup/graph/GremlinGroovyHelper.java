package org.jboss.windup.graph;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;

/**
 * This is a helper class for executing Groovy Gremlin scripts.
 */
public class GremlinGroovyHelper
{
    public static final String EMBEDDED_SCRIPT_PREFIX = "#{";
    public static final String EMBEDDED_SCRIPT_POSTFIX = "}";

    private static final String IT = "it";
    private static final String G = "g";

    private final static GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();

    /**
     * Find all Gremlin-Groovy scripts in the provided inputString that start with {@link GremlinGroovyHelper#EMBEDDED_SCRIPT_PREFIX} and end with
     * {@link GremlinGroovyHelper#EMBEDDED_SCRIPT_POSTFIX}, and replace them with the results of executing the gremlin content.
     */
    public static String evaluateEmbeddedScripts(GraphContext graphContext, Vertex vertex, String inputString)
    {
        if (StringUtils.isBlank(inputString))
        {
            return inputString;
        }

        StringBuilder result = new StringBuilder();
        int startIdx = inputString.indexOf(EMBEDDED_SCRIPT_PREFIX);
        int endIdx = -1;
        if (startIdx == -1)
        {
            return inputString;
        }

        while (startIdx != -1)
        {
            result.append(inputString.substring(endIdx + 1, startIdx));
            endIdx = inputString.indexOf(EMBEDDED_SCRIPT_POSTFIX, startIdx);
            String scriptString = inputString.substring(startIdx + EMBEDDED_SCRIPT_PREFIX.length(), endIdx);
            Object scriptResult = executeGremlinScript(graphContext, vertex, scriptString);
            String scriptResultString = scriptResult == null ? "" : scriptResult.toString();
            result.append(scriptResultString);
            startIdx = inputString.indexOf(EMBEDDED_SCRIPT_PREFIX, startIdx + 1);
        }

        if (endIdx != -1 && endIdx != inputString.length())
        {
            result.append(inputString.substring(endIdx + 1));
        }

        return result.toString();
    }

    /**
     * Executes the provided script with the provided {@link GraphContext}. Scripts should use {@link GremlinGroovyHelper#G} to access the entire
     * graph.
     */
    public static synchronized Object executeGremlinScript(GraphContext graphContext, String scriptString)
    {
        return executeGremlinScript(graphContext, scriptString);
    }

    /**
     * Executes the provided script with the provided {@link GraphContext} and starting {@link Vertex}. Scripts should use
     * {@link GremlinGroovyHelper#G} to access the entire graph or {@link GremlinGroovyHelper#IT} to access the current {@link Vertex}.
     */
    public static synchronized Object executeGremlinScript(GraphContext graphContext, Vertex vertex, String scriptString)
    {
        try
        {
            final CompiledScript script = engine.compile(scriptString);
            Bindings bindings = engine.createBindings();
            if (vertex != null)
            {
                bindings.put(IT, vertex);
            }
            bindings.put(G, graphContext.getFramed());
            return script.eval(bindings);
        }
        catch (ScriptException e)
        {
            throw new WindupException("Could not execute groovy script due to: " + e.getMessage(), e);
        }
    }
}
