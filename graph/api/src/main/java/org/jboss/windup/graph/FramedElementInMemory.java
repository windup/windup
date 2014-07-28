package org.jboss.windup.graph;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;

/**
 * This class represents the @Property elements of a framed element in memory.
 * 
 * This in memory representation can be attached to the graph by calling "attach(FramedGraph)"
 * 
 */
public class FramedElementInMemory<T extends VertexFrame> implements InvocationHandler
{
    private final Class<T> type;
    private Map<String, Object> values = new HashMap<>();

    public FramedElementInMemory(Class<T> type)
    {
        this.type = type;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] arguments)
    {
        String methodName = method.getName();
        if (methodName.equals("hashCode"))
        {
            return this.hashCode();
        }
        else if (methodName.equals("equals"))
        {
            return this.equals(arguments[0]);
        }
        else if (methodName.equals("toString"))
        {
            return this.toString();
        }
        else if (methodName.equals("attach"))
        {
            FramedGraph<?> framedGraph = (FramedGraph<?>) arguments[0];
            attach(framedGraph);
            return null;
        }

        Property propertyAnnotation = method.getAnnotation(Property.class);
        if (propertyAnnotation == null)
        {
            throw new WindupException("Method " + methodName
                        + " called, but has no @Property annotation... only @Property methods are supported");
        }

        String propertyName = propertyAnnotation.value();

        if (ClassUtilities.isGetMethod(method))
        {
            return values.get(propertyName);
        }
        else if (ClassUtilities.isSetMethod(method))
        {
            Object value = arguments[0];
            if (value == null)
            {
                values.remove(propertyName);
            }
            else
            {
                values.put(propertyName, value);
            }
            return null;
        }
        else if (ClassUtilities.isRemoveMethod(method))
        {
            values.remove(propertyName);
            return null;
        }
        else
        {
            throw new WindupException("Unrecognized method " + methodName + " called on in-memory Frame!");
        }
    }

    private void attach(FramedGraph<?> framed)
    {
        T element = framed.addVertex(null, this.type);
        Vertex v = element.asVertex();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            v.setProperty(entry.getKey(), entry.getValue());
        }
    }
}
