package com.tinkerpop.frames.modules.javahandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.util.ExceptionUtils;
import javassist.util.proxy.ProxyFactory;

/**
 * Calls the methods annotated with {@link Initializer} on frame
 * implementations.
 * 
 * @author Bryn Cooke
 * 
 */
class JavaFrameInitializer implements FrameInitializer {

	private JavaHandlerModule module;

    /**
     * Caches lists of InitializerMethods for a given frame class.  See "doLoad" method below.
     */
    private LoadingCache<Class<?>, List<InitializerMethod>> initializerCache = CacheBuilder
            .newBuilder().build(new CacheLoader<Class<?>, List<InitializerMethod>>() {
                @Override
                public List<InitializerMethod> load(final Class<?> frameClass) throws Exception {
                    return doLoad(frameClass);
                }
            });


    JavaFrameInitializer(JavaHandlerModule module) {
        this.module = module;
    }

    /**
     * Captures the work of calling an "@Initializer" annotated method.
     */
    private class InitializerMethod {
        private final Class<?> h;
        private final Method method;

        private InitializerMethod(Class<?> h, Method method) {
            this.h = h;
            this.method = method;
        }

        void execute(Object framedElement, FramedGraph<?> framedGraph, Element element)
                throws InvocationTargetException, IllegalAccessException {
            Object handler = module.createHandler(framedElement, framedGraph, element, h, method);
            method.invoke(handler);
        }
    }

	@Override
	public void initElement(Class<?> kind, FramedGraph<?> framedGraph, Element element) {

		Object framedElement;
		if (element instanceof Vertex) {
			framedElement = framedGraph.frame((Vertex) element, kind);
		} else {
			framedElement = framedGraph.frame((Edge) element, kind);
		}

        try {
            for (InitializerMethod method : initializerCache.get(kind)) {
                try {
                    method.execute(framedElement, framedGraph, element);
                } catch (IllegalArgumentException e) {
                    throw new JavaHandlerException("Problem calling Java handler", e);
                } catch (IllegalAccessException e) {
                    throw new JavaHandlerException("Problem calling Java handler", e);
                } catch (InvocationTargetException e) {
                    ExceptionUtils.sneakyThrow(e.getTargetException());
                }
            }
        } catch (ExecutionException e) {
            throw new JavaHandlerException("Problem calling Java handler", e);
        }

    }

    /**
     * Finds all the relevant @Initializer methods for the given class.
     */
    private List<InitializerMethod> doLoad(Class<?> kind) {
        // We have to order this correctly. Dependencies should be initialised
        // first so we first recursively collect an an array of classes to call
        // and then reverse the array before putting them in a linked hash set.
        // That way the classes discovered last will be called first.
        List<Class<?>> classes = new ArrayList<Class<?>>();
        depthFirstClassSearch(classes, kind);

        Collections.reverse(classes);
        LinkedHashSet<Class<?>> hierarchy = new LinkedHashSet<Class<?>>(classes);
        List<InitializerMethod> methods = Lists.newArrayList();

        // Now we can store InitializerMethod objects for each method call.
        for (Class<?> h : hierarchy) {
            try {
                try {
                    Class<?> implKind = module.getHandlerClass(h);
                    for (Method method : implKind.getDeclaredMethods()) {
                        if (method.isAnnotationPresent(Initializer.class)) {
                            if (method.getParameterTypes().length != 0) {
                                throw new JavaHandlerException("Java handler initializer " + method + "cannot have parameters");
                            }
                            methods.add(new InitializerMethod(h, method));
                        }

                    }
                } catch (ClassNotFoundException e) {
                    // There was no impl class to check
                }
            } catch (IllegalArgumentException e) {
                throw new JavaHandlerException("Problem calling Java handler", e);
            }
        }

        return methods;
    }

    private void depthFirstClassSearch(List<Class<?>> initializers, Class<?> kind) {

        if (kind == null || kind == Object.class) {
            return;
        }

        initializers.add(kind);

        for (Class<?> i : kind.getInterfaces()) {
            depthFirstClassSearch(initializers, i);
        }
        depthFirstClassSearch(initializers, kind.getSuperclass());

    }
}
