package org.jboss.windup.graph.javahandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.syncleus.ferma.ReflectionCache;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.graph.MapInAdjacentPropertiesHandler;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

/**
 * Provides the implementation for the {@link JavaHandler} annotation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaHandlerHandler extends AbstractMethodHandler implements MethodHandler
{
    private static Map<Class<?>, Object> objectCache = new HashMap<>();
    private static Map<String, Method> methodCache = new HashMap<>();

    @Override
    public Class<JavaHandler> getAnnotationType()
    {
        return JavaHandler.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation)
    {
        return createInterceptor(builder, method);
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method)
    {
        return builder.define(method).intercept(MethodDelegation.to(JavaHandlerHandler.JavaHandlerInterceptor.class))
                .annotateMethod(method.getAnnotations());
    }

    public static final class JavaHandlerInterceptor
    {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments Object[] args)
        {
            ReflectionCache reflectionCache = ((CachesReflection) thisFrame).getReflectionCache();
            final JavaHandler ann = reflectionCache.getAnnotation(method, JavaHandler.class);

            try
            {
                Class<?> handlerClass = ann.handler();
                Object handler = getHandlerInstance(handlerClass);
                Method handlerMethod = findMethodHandler(handlerClass, method.getName());
                if (handlerMethod == null)
                    throw new WindupException("Could not find method on handler with name: " + method.getName());

                // If there is one additional parameter, assume that the first parameter should be the frame itself
                if (handlerMethod.getParameterTypes().length == (args.length+1))
                {
                    List<Object> newArgs = new ArrayList<>();
                    newArgs.add(thisFrame);
                    newArgs.addAll(Arrays.asList(args));
                    args = newArgs.toArray(new Object[newArgs.size()]);
                }

                return handlerMethod.invoke(handler, args);
            }
            catch (IllegalAccessException | InstantiationException | InvocationTargetException e)
            {
                throw new WindupException(e);
            }
        }

        private static Object getHandlerInstance(Class<?> clazz) throws IllegalAccessException, InstantiationException
        {
            synchronized (objectCache)
            {
                Object result = objectCache.get(clazz);
                if (result == null)
                {
                    result = clazz.newInstance();
                    objectCache.put(clazz, result);
                }
                return result;
            }
        }

        /**
         * NOTE: Polymorphism is not currently supported.
         */
        private static Method findMethodHandler(Class handlerClass, String methodName)
        {
            synchronized (methodCache)
            {
                String key = handlerClass.getCanonicalName() + "_" + methodName;
                Method method = methodCache.get(key);
                if (method == null)
                {
                    for (Method candidateMethod : handlerClass.getMethods())
                    {
                        if (candidateMethod.getName().equals(methodName))
                        {
                            method = candidateMethod;
                            methodCache.put(key, method);
                            break;
                        }
                    }
                }
                return method;
            }
        }
    }
}
