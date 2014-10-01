package com.tinkerpop.frames.modules;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FramedGraph;

/**
 * Allows handling of method on frames. Only the first method handler found is called.
 * Instances of this class should be threadsafe.
 * 
 * @param <T> The type of annotation handled.
 */
public interface MethodHandler<T extends Annotation> {
    /**
     * @return The annotation type that this handler responds to. 
     */
    public Class<T> getAnnotationType();

    /**
     * @param frame The frame upon which the method is being called.
     * @param method The method being called on the frame.
     * @param arguments The arguments to the method.
     * @param annotation The annotation
     * @param framedGraph The graph being called. 
     * @param element The underlying element.
     * @return A return value for the method.
     */
    public Object processElement(final Object frame, final Method method, final Object[] arguments, final T annotation, final FramedGraph<?> framedGraph, final Element element);
}
