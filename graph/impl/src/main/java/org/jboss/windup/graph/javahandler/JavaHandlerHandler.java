package org.jboss.windup.graph.javahandler;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.jboss.windup.graph.JavaHandler;
import org.jboss.windup.util.exception.WindupException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides the implementation for the {@link JavaHandler} annotation.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class JavaHandlerHandler extends AbstractMethodHandler implements MethodHandler {
    @Override
    public Class<JavaHandler> getAnnotationType() {
        return JavaHandler.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return createInterceptor(builder, method);
    }

    private <E> DynamicType.Builder<E> createInterceptor(final DynamicType.Builder<E> builder, final Method method) {
//        return builder.method(ElementMatchers.is(method))
//                    .intercept(MethodDelegation.to(JavaHandlerHandler.JavaHandlerInterceptor.class));
        return builder.define(method).intercept(MethodDelegation.to(JavaHandlerHandler.JavaHandlerInterceptor.class))
                .annotateMethod(method.getAnnotations());
    }

    public static final class JavaHandlerInterceptor {
        @RuntimeType
        public static Object execute(@This final ElementFrame thisFrame, @Origin final Method method, @RuntimeType @AllArguments Object[] args) {
            ReflectionCache reflectionCache = ((CachesReflection) thisFrame).getReflectionCache();
            final JavaHandler ann = reflectionCache.getAnnotation(method, JavaHandler.class);

            try {
                Class<?> handlerClass = ann.handler();
                Method handlerMethod = findMethodHandler(method, handlerClass);
                if (handlerMethod == null)
                    throw new WindupException("Could not find method on handler with name: " + method.getName());

                Object handler = handlerClass.newInstance();

                // If there is one additional parameter, assume that the first parameter should be the frame itself
                if (handlerMethod.getParameterTypes().length == (args.length + 1)) {
                    List<Object> newArgs = new ArrayList<>();
                    newArgs.add(thisFrame);
                    newArgs.addAll(Arrays.asList(args));
                    args = newArgs.toArray(new Object[newArgs.size()]);
                }

                return handlerMethod.invoke(handler, args);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                throw new WindupException(e);
            }
        }

        /**
         * NOTE: Polymorphism is not currently supported.
         */
        private static Method findMethodHandler(Method originalMethod, Class handlerClass) {
            for (Method candidateMethod : handlerClass.getMethods()) {
                if (candidateMethod.getName().equals(originalMethod.getName()))
                    return candidateMethod;
            }
            return null;
        }
    }
}
