package org.jboss.windup.graph;

import java.lang.reflect.Method;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;

/**
 * Returns "this", so you can do things like frame.setFoo(123).setBar(456).
 */
public class WindupPropertyMethodHandler implements MethodHandler<Property>
{
    @Override
    public Class<Property> getAnnotationType()
    {
        return Property.class;
    }

    @Override
    public Object processElement(Object frame, Method method, Object[] arguments, Property annotation,
                FramedGraph<?> framedGraph, Element element)
    {
        // Getters
        if (ClassUtilities.isGetMethod(method))
        {
            Object value = element.getProperty(annotation.value());
            if (method.getReturnType().isEnum())
                return getValueAsEnum(method, value);
            else
                return value;
        }
        // Setters
        else if (ClassUtilities.isSetMethod(method))
        {
            Object value = arguments[0];
            if (null == value)
            {
                element.removeProperty(annotation.value());
            }
            else
            {
                if (value.getClass().isEnum())
                    element.setProperty(annotation.value(), ((Enum<?>) value).name());
                else
                    element.setProperty(annotation.value(), value);
            }
            return frame;
        }
        else if (ClassUtilities.isRemoveMethod(method))
        {
            element.removeProperty(annotation.value());
            return frame;
        }

        return frame;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Enum<?> getValueAsEnum(final Method method, final Object value)
    {
        Class<?> returnType = method.getReturnType();
        if (Enum.class.isAssignableFrom(returnType))
        {
            Class<Enum<?>> en = (Class<Enum<?>>) returnType;

            if (value != null)
                return Enum.valueOf((Class) en, value.toString());
        }
        throw new IllegalArgumentException("Method does not return an Enum type.");
    }
}
