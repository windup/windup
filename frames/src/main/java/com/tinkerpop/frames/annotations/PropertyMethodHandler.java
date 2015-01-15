package com.tinkerpop.frames.annotations;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.MethodHandler;

import java.lang.reflect.Method;

public class PropertyMethodHandler implements MethodHandler<Property> {

    @Override
    public Class<Property> getAnnotationType() {
        return Property.class;
    }

    @Override
    public Object processElement(Object frame, Method method,
            Object[] arguments, Property annotation,
            FramedGraph<?> framedGraph, Element element) {
        if (ClassUtilities.isGetMethod(method)) {
            Object value = element.getProperty(annotation.value());
            if (method.getReturnType().isEnum())
                return getValueAsEnum(method, value);
            else
                return value;
        } else if (ClassUtilities.isSetMethod(method)) {
            Object value = arguments[0];
            if (null == value) {
                element.removeProperty(annotation.value());
            } else {
                if (value.getClass().isEnum()) {
                    element.setProperty(annotation.value(), ((Enum<?>) value).name());
                } else {
                    element.setProperty(annotation.value(), value);
                }
            }
            if (method.getReturnType().isAssignableFrom(frame.getClass()))
        	return frame;
        } else if (ClassUtilities.isRemoveMethod(method)) {
            element.removeProperty(annotation.value());
            return null;
        }

        return null;
    }

    private Enum getValueAsEnum(final Method method, final Object value) {
        Class<Enum> en = (Class<Enum>) method.getReturnType();
        if (value != null)
            return Enum.valueOf(en, value.toString());

        return null;
    }
}
