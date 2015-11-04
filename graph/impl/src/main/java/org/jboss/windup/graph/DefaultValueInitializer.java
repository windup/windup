package org.jboss.windup.graph;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.frames.FrameInitializer;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.Property;
import org.jboss.windup.graph.frames.FrameBooleanDefaultValue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * A tinkerpop frame initializer that makes it possible to specify default values for the elements
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class DefaultValueInitializer implements FrameInitializer
{
    private Map<Class<?>, LinkedList<PropertyDefaultValue>> cachedValues = new HashMap<>();

    public void initElement(final Class<?> kind, final FramedGraph<?> framedGraph, final Element element)
    {
        if (!cachedValues.containsKey(kind))
        {
            cacheFrameInterface(kind);
        }
        setupDefaults(element, cachedValues.get(kind));
    }

    private void cacheFrameInterface(Class<?> kind) {
        LinkedList<PropertyDefaultValue> values = new LinkedList<>();
        for (Method m : kind.getMethods())
        {
            Annotation[] annotations = m.getAnnotations();
            for (Annotation annotation : m.getAnnotations())
            {
                if (annotation instanceof FrameBooleanDefaultValue)
                {
                    PropertyDefaultValue pDefault = new PropertyDefaultValue();
                    pDefault.value = ((FrameBooleanDefaultValue) annotation).value();
                    pDefault.key = m.getAnnotation(Property.class).value();
                    values.add(pDefault);
                }
            }
        }
        cachedValues.put(kind, values);
    }

    private void setupDefaults(Element element, LinkedList<PropertyDefaultValue> values)
    {
        for (PropertyDefaultValue pValue : values)
        {
            element.setProperty(pValue.key, pValue.value);
        }
    }

    private class PropertyDefaultValue
    {
        private String key;
        private Object value;

    }
}