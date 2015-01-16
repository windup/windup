package org.jboss.windup.graph;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jboss.windup.graph.model.InMemoryVertexFrame;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.ClassUtilities;
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
    private final GraphContext context;
    private final Class<T> type;
    private final Map<String, Object> values = new HashMap<>();

    private static final Method attachMethod;
    private static final Method hashCodeMethod;
    private static final Method equalsMethod;
    private static final Method toStringMethod;

    static
    {
        try
        {
            attachMethod = InMemoryVertexFrame.class.getMethod("attachToGraph");
            hashCodeMethod = Object.class.getMethod("hashCode");
            equalsMethod = Object.class.getMethod("equals", new Class[] { Object.class });
            toStringMethod = Object.class.getMethod("toString");
        }
        catch (NoSuchMethodException e)
        {
            throw new WindupException(e.getMessage(), e);
        }
    }

    public FramedElementInMemory(GraphContext graphContext, Class<T> type)
    {
        this.context = graphContext;
        this.type = type;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] arguments)
    {
        String methodName = method.getName();
        if (method.equals(hashCodeMethod))
        {
            return this.hashCode();
        }
        else if (method.equals(equalsMethod))
        {
            return this.equals(arguments[0]);
        }
        else if (method.equals(toStringMethod))
        {
            return this.toString();
        }
        else if (method.equals(attachMethod))
        {
            attach();
            return null;
        }

        String propertyName;
        Property propertyAnnotation = method.getAnnotation(Property.class);
        if (propertyAnnotation == null)
        {
            Property windupPropertyAnnnotation = method.getAnnotation(Property.class);
            if (windupPropertyAnnnotation == null)
            {
                throw new WindupException("Method " + methodName
                            + " called, but has no @Property annotation... only @Property methods are supported");
            }
            else
            {
                propertyName = windupPropertyAnnnotation.value();
            }
        }
        else
        {
            propertyName = propertyAnnotation.value();
        }

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
            return proxy;
        }
        else if (ClassUtilities.isRemoveMethod(method))
        {
            values.remove(propertyName);
            return proxy;
        }
        else
        {
            throw new WindupException("Unrecognized method " + methodName + " called on in-memory Frame!");
        }
    }

    private void attach()
    {
        T element = context.getFramed().addVertex(null, this.type);
        Vertex v = element.asVertex();
        for (Map.Entry<String, Object> entry : values.entrySet())
        {
            v.setProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String toString()
    {
        return "[Proxy for: " + type.getCanonicalName() + ", values: " + values + "]";
    }
}
