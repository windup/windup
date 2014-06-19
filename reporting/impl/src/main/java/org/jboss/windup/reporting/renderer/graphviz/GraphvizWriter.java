package org.jboss.windup.reporting.renderer.graphviz;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.jboss.windup.reporting.renderer.dot.DotWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Graph;

public class GraphvizWriter extends DotWriter {
	private static final Logger LOG = LoggerFactory.getLogger(GraphvizWriter.class);

	private final CompiledScript vizJsCompiled;
	
	public GraphvizWriter(Graph graph) throws ScriptException, IOException {
		super(graph);
		if(true) {
			throw new RuntimeException("Not yet implemented; this is unstable.  The javascript requires Int32Array, which isn't currently provided by Rhino.");
		}
		
		//precompile the javascript.
		StringBuilder builder = new StringBuilder();
		builder.append(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("vizjs/viz.js")));
		
		builder = new StringBuilder();
		builder.append("function Viz() { console.log('Hello World'); }");
		
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("JavaScript");
		
		
		String script = builder.toString();
		Compilable compilingEngine = (Compilable)engine;
		vizJsCompiled = compilingEngine.compile(builder.toString());
	}
	
	@Override
	public void writeGraph(OutputStream os) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		super.writeGraph(baos);

		try {
			StringBuilder builder = new StringBuilder();
			builder.append(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("vizjs/viz.js")));
			builder.append("var result = new Viz(dotGraph);");
			
			String script = builder.toString();
	        ScriptEngine engine =  new ScriptEngineManager().getEngineByName("JavaScript");
	        Compilable compilingEngine = (Compilable) engine;
	        CompiledScript cscript = compilingEngine.compile(script);

	        //Bindings bindings = cscript.getEngine().createBindings();
	        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
	        for(Map.Entry me : bindings.entrySet()) {
	            System.out.printf("%s: %s\n",me.getKey(),String.valueOf(me.getValue()));
	        }
	        bindings.put("dotGraph", baos.toString());
	        //cscript.eval();
	        Object result = cscript.eval(bindings);
	        LOG.info("Result:" +ReflectionToStringBuilder.toString(result));
		} catch (Exception e) {
			throw new IOException("Exception generating graph.", e);
		}
	}
}
