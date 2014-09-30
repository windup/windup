package com.tinkerpop.frames.annotations.gremlin;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Logger;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.annotations.AnnotationHandler;
import com.tinkerpop.frames.modules.MethodHandler;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.frames.structures.FramedVertexMap;
import com.tinkerpop.frames.util.ExceptionUtils;
import com.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.iterators.SingleIterator;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Bryn Cooke
 */
public class GremlinGroovyAnnotationHandler implements AnnotationHandler<GremlinGroovy>, MethodHandler<GremlinGroovy> {
    private static final String PIPE = "_()";
    private final GremlinGroovyScriptEngine engine = new GremlinGroovyScriptEngine();
    private static final String IT = "it";
    private static final String G = "g";

    private static final Logger LOGGER = Logger.getLogger(GremlinGroovyAnnotationHandler.class.getName());

    public GremlinGroovyScriptEngine getGremlinScriptEngine() {
        return this.engine;
    }

    @Override
    public Class<GremlinGroovy> getAnnotationType() {
        return GremlinGroovy.class;
    }

    @Override
    public Object processElement(final GremlinGroovy annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph,
                                 final Element element, final Direction direction) {
        if (element instanceof Vertex) {
            return processVertex(annotation, method, arguments, framedGraph, (Vertex) element);
        } else {
            throw new UnsupportedOperationException("This method only works for vertices");
        }
    }

    public Object processVertex(final GremlinGroovy annotation, final Method method, final Object[] arguments, final FramedGraph framedGraph,
                                final Vertex vertex) {
        try {
            final CompiledScript script = this.engine.compile(annotation.value());
            final Bindings bindings = getBindings(method, arguments);
            bindings.put(IT, vertex);
            bindings.put(G, framedGraph);
            final Object result = script.eval(bindings);

            // TODO: Deprecate the use of _() and replace with it
            if (result instanceof Pipe & annotation.value().startsWith(PIPE)) {
                LOGGER.warning("_() is deprecated in favor of using 'it' to represent the framed vertex");
                ((Pipe) result).setStarts(new SingleIterator<Element>(vertex));
            }

            if (annotation.frame()) {
                if (result instanceof Iterable) {
                    final FramedVertexIterable r = new FramedVertexIterable(framedGraph, (Iterable) result, ClassUtilities.getGenericClass(method));
                    return (ClassUtilities.returnsIterable(method)) ? r : r.iterator().hasNext() ? r.iterator().next() : null;
                } else if (ClassUtilities.returnsMap(method)) {
                    return new FramedVertexMap(framedGraph, (Map) result, ClassUtilities.getGenericClass(method));
                } else if (result instanceof Vertex) {
                    return framedGraph.frame((Vertex) result, ClassUtilities.getGenericClass(method));
                } else {
                    throw new IllegalStateException("The returned object can not be framed: " + result.getClass());
                }
            } else {
                return result;
            }

        } catch (ScriptException e) {
        	ExceptionUtils.sneakyThrow(e); //Preserve original exception functionality.
            return null;
        }
    }

    private Bindings getBindings(final Method method, final Object[] arguments) {
        Bindings bindings = engine.createBindings();
        Annotation[][] allParameterAnnotations = method.getParameterAnnotations();
        for (int pCount = 0; pCount < allParameterAnnotations.length; pCount++) {
            Annotation parameterAnnotations[] = allParameterAnnotations[pCount];
            for (int aCount = 0; aCount < parameterAnnotations.length; aCount++) {
                Annotation paramAnnotation = parameterAnnotations[aCount];
                if (paramAnnotation instanceof GremlinParam) {
                    bindings.put(((GremlinParam) paramAnnotation).value(), arguments[pCount]);
                    break;
                }
            }
        }
        return bindings;
    }

	@Override
	public Object processElement(Object framedElement, Method method,
			Object[] arguments, GremlinGroovy annotation,
			FramedGraph<?> framedGraph, Element element) {
		if (element instanceof Vertex) {
            return processVertex(annotation, method, arguments, framedGraph, (Vertex) element);
        } else {
            throw new UnsupportedOperationException("This method only works for vertices");
        }
	}


}
