package org.jboss.windup.graph;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.syncleus.ferma.framefactories.annotation.AbstractMethodHandler;

import com.syncleus.ferma.ElementFrame;
import com.syncleus.ferma.framefactories.annotation.CachesReflection;
import com.syncleus.ferma.framefactories.annotation.ReflectionUtility;

import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Argument;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public class WindupPropertyMethodHandler extends AbstractMethodHandler {
    @Override
    public Class<Property> getAnnotationType() {
        return Property.class;
    }

    @Override
    public <E> DynamicType.Builder<E> processMethod(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        final java.lang.reflect.Parameter[] arguments = method.getParameters();

        if (ReflectionUtility.isSetMethod(method))
            if (arguments == null || arguments.length == 0)
                throw new IllegalStateException(method.getName() + " was annotated with @Property but had no arguments.");
            else if (arguments.length == 1)
                return this.setProperty(builder, method, annotation);
            else
                throw new IllegalStateException(method.getName() + " was annotated with @Property but had more than 1 arguments.");
        else if (ReflectionUtility.isGetMethod(method))
            if (arguments == null || arguments.length == 0)
                return this.getProperty(builder, method, annotation);
            else
                throw new IllegalStateException(method.getName() + " was annotated with @Property but had arguments.");
        else if (ReflectionUtility.isRemoveMethod(method))
            if (arguments == null || arguments.length == 0)
                return this.removeProperty(builder, method, annotation);
            else
                throw new IllegalStateException(method.getName() + " was annotated with @Property but had some arguments.");
        else
            throw new IllegalStateException(
                    method.getName() + " was annotated with @Property but did not begin with either of the following keywords: add, get");
    }

    private <E> DynamicType.Builder<E> setProperty(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(WindupPropertyMethodHandler.SetPropertyInterceptor.class));
    }

    private <E> DynamicType.Builder<E> getProperty(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(WindupPropertyMethodHandler.GetPropertyInterceptor.class));
    }

    private <E> DynamicType.Builder<E> removeProperty(final DynamicType.Builder<E> builder, final Method method, final Annotation annotation) {
        return builder.method(ElementMatchers.is(method)).intercept(MethodDelegation.to(WindupPropertyMethodHandler.RemovePropertyInterceptor.class));
    }

    private static Enum getValueAsEnum(final Method method, final Object value) {
        final Class<Enum> en = (Class<Enum>) method.getReturnType();
        if (value != null)
            return Enum.valueOf(en, value.toString());

        return null;
    }

    public static final class GetPropertyInterceptor {

        @RuntimeType
        public static Object getProperty(@This final ElementFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Property annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Property.class);
            final String value = annotation.value();

            final Object obj = thiz.getProperty(value);
            if (method.getReturnType().isEnum())
                return getValueAsEnum(method, obj);
            else
                return obj;
        }
    }

    public static final class SetPropertyInterceptor {
        @RuntimeType
        public static void setProperty(@This final ElementFrame thiz, @Origin final Method method, @RuntimeType @Argument(0) final Object obj) {
            assert thiz instanceof CachesReflection;
            final Property annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Property.class);
            final String propertyName = annotation.value();
            final Object propertyValue;
            if ((obj != null) && (obj.getClass().isEnum()))
                propertyValue = ((Enum<?>) obj).name();
            else
                propertyValue = obj;

            if (propertyValue == null) {
                RemovePropertyInterceptor.removeProperty(thiz, method);
                return;
            }

            Element element = thiz.getElement();
            if (element instanceof Vertex)
                thiz.getGraph().getRawTraversal().V(element.id()).property(propertyName, propertyValue).iterate();
            else
                thiz.getGraph().getRawTraversal().E(element.id()).property(propertyName, propertyValue).iterate();
        }
    }

    public static final class RemovePropertyInterceptor {

        public static void removeProperty(@This final ElementFrame thiz, @Origin final Method method) {
            assert thiz instanceof CachesReflection;
            final Property annotation = ((CachesReflection) thiz).getReflectionCache().getAnnotation(method, Property.class);
            final String propertyName = annotation.value();
            Element element = thiz.getElement();
            if (element instanceof Vertex)
                thiz.getGraph().getRawTraversal().V(element.id()).properties(propertyName).drop().iterate();
            else
                thiz.getGraph().getRawTraversal().E(element.id()).properties(propertyName).drop().iterate();

            thiz.getElement().property(propertyName).remove();
        }
    }
}
