/**
 * Copyright 2004 - 2017 Syncleus, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.windup.graph;

import com.syncleus.ferma.AbstractEdgeFrame;
import com.syncleus.ferma.AbstractVertexFrame;
import com.syncleus.ferma.EdgeFrame;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.framefactories.FrameFactory;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FieldAccessor;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AbstractAnnotationFrameFactory implements FrameFactory {
    protected final Map<Class<? extends Annotation>, MethodHandler> methodHandlers = new HashMap<>();
    private final ClassLoader classLoader;
    private final ReflectionCache reflectionCache;
    private final Map<Class, Class> constructedClassCache = new HashMap<>();

    protected AbstractAnnotationFrameFactory(final ClassLoader classLoader, final ReflectionCache reflectionCache, Set<MethodHandler> handlers) {
        this.classLoader = classLoader;
        this.reflectionCache = reflectionCache;
        for (MethodHandler handler : handlers)
            this.methodHandlers.put(handler.getAnnotationType(), handler);
    }

    private static boolean isAbstract(final Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }

    private static boolean isAbstract(final Method method) {
        return Modifier.isAbstract(method.getModifiers());
    }

    @Override
    public <T> T create(final Element e, final Class<T> kind) {

        Class<? extends T> resolvedKind = kind;
        if (isAbstract(resolvedKind))
            resolvedKind = constructClass(e, kind);
        try {
            final T object = resolvedKind.newInstance();
            if (object instanceof CachesReflection)
                ((CachesReflection) object).setReflectionCache(this.reflectionCache);
            return object;
        } catch (final InstantiationException | IllegalAccessException caught) {
            throw new IllegalArgumentException("kind could not be instantiated", caught);
        }
    }

    private <E> Class<? extends E> constructClass(final Element element, final Class<E> clazz) {
        Class constructedClass = constructedClassCache.get(clazz);
        if (constructedClass != null)
            return constructedClass;

        DynamicType.Builder<? extends E> classBuilder;
        if (clazz.isInterface()) {
            if (element instanceof Vertex)
                classBuilder = (DynamicType.Builder<? extends E>) new ByteBuddy().subclass(AbstractVertexFrame.class);
            else if (element instanceof Edge)
                classBuilder = (DynamicType.Builder<? extends E>) new ByteBuddy().subclass(AbstractEdgeFrame.class);
            else
                throw new IllegalStateException("class is neither an Edge or a vertex!");

            if (clazz.getCanonicalName().contains("ByteBuddy")) {
                // if the input class is itself a bytebuddy class, only take its interfaces
                classBuilder = classBuilder.implement(clazz.getInterfaces());
            } else {
                classBuilder = classBuilder.implement(clazz);
            }
        } else {
            if (!(element instanceof Vertex || element instanceof Edge))
                throw new IllegalStateException("element is neither an edge nor a vertex");
            else if (element instanceof Vertex && !VertexFrame.class.isAssignableFrom(clazz))
                throw new IllegalStateException(clazz.getName() + " Class is not a type of VertexFrame");
            else if (element instanceof Edge && !EdgeFrame.class.isAssignableFrom(clazz))
                throw new IllegalStateException(clazz.getName() + " Class is not a type of EdgeFrame");
            classBuilder = new ByteBuddy().subclass(clazz);
        }

        classBuilder = classBuilder.defineField("reflectionCache", ReflectionCache.class, Visibility.PRIVATE, FieldManifestation.PLAIN)
                .implement(CachesReflection.class).intercept(FieldAccessor.ofBeanProperty());

        /*
         * Just a hack so that our generified frame types can work.
         *
         * This information will not really be used by the generated class.
         */
        classBuilder = classBuilder.typeVariable("T");

        // try and construct any abstract methods that are left
        for (final Method method : clazz.getMethods())
            if (isAbstract(method))
                annotation_loop:for (final Annotation annotation : method.getAnnotations()) {
                    final MethodHandler handler = methodHandlers.get(annotation.annotationType());
                    if (handler != null) {
                        classBuilder = handler.processMethod(classBuilder, method, annotation);
                        break;
                    }
                }

        DynamicType.Unloaded unloadedClass = classBuilder.make();
        constructedClass = unloadedClass.load(this.classLoader, ClassLoadingStrategy.Default.WRAPPER).getLoaded();
        this.constructedClassCache.put(clazz, constructedClass);
        return constructedClass;
    }
}
