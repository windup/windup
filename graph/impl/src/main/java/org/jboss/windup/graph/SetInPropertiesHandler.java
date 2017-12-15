package org.jboss.windup.graph;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;

/**
 * Handles @SetInProperties Set<String,String>.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
public class SetInPropertiesHandler implements MethodHandler<SetInProperties>
{
    private static final Logger log = Logging.get(SetInPropertiesHandler.class);

    private static final String SET_VERTEX_PROP_VALUE = "1";


    @Override
    public Class<SetInProperties> getAnnotationType()
    {
        return SetInProperties.class;
    }


    /**
     * The handling method.
     */
    @Override
    public Object processElement(Object frame, Method method, Object[] args, SetInProperties ann,
                FramedGraph<?> framedGraph, Element elm)
    {
        String methodName = method.getName();
        if (methodName.startsWith("get"))
            return handleGetter((Vertex) elm, method, args, ann, framedGraph);

        else if (methodName.startsWith("set"))
            handleSetter((Vertex) elm, method, args, ann, framedGraph);

        else if (methodName.startsWith("addAll"))
            handleAddAll((Vertex) elm, method, args, ann, framedGraph);

        else if (methodName.startsWith("add"))
            handleAdder((Vertex) elm, method, args, ann, framedGraph);

        else
            throw new WindupException("Only get*, set*, add*, and addAll* method names are supported for @"
                    + SetInProperties.class.getSimpleName() + ", found at: " + method.getName());

        return frame;
    }

    /**
     * Getter
     */
    private Set<String> handleGetter(Vertex vertex, Method method, Object[] args, SetInProperties ann, FramedGraph<?> framedGraph)
    {
        if (args != null && args.length != 0)
            throw new WindupException("Method must take zero arguments");

        Set<String> set = new HashSet<>();
        String prefix = preparePrefix(ann);

        Set<String> keys = vertex.getPropertyKeys();
        for (String key : keys)
        {
            String tail = key;
            if (!prefix.isEmpty())
            {
                if(!key.startsWith(prefix))
                    continue;
                else
                    tail = key.substring(prefix.length());
            }

            set.add(tail);
        }

        return set;
    }

    /**
     * Setter
     */
    private void handleSetter(Vertex vertex, Method method, Object[] args, SetInProperties ann, FramedGraph<?> framedGraph)
    {
        // Argument.
        if (args == null || args.length != 1)
            throw new WindupException("Method must take one argument: " + method.getName());

        if (!(args[0] instanceof Set))
            throw new WindupException("Argument of " + method.getName() + " must be a Set<String>, but is: " + args[0].getClass());

        @SuppressWarnings("unchecked")
        Set<String> newSet = (Set<String>) args[0];

        String prefix = preparePrefix(ann);

        // For all keys in the old set...
        Set<String> vertKeys = vertex.getPropertyKeys();
        for (String vertKey : vertKeys)
        {
            if (!vertKey.startsWith(prefix))
                continue;
            if (WindupVertexFrame.TYPE_PROP.equals(vertKey)) // Leave the "type" property.
                continue;
            if (vertKey.startsWith("w:")) // Leave windup internal properties. TODO: Get the prefix from somewhere.
                continue;

            String subKey = vertKey.substring(prefix.length());
            // ...either change to the new value,
            if (newSet.contains(subKey))
            {
                vertex.setProperty(vertKey, SET_VERTEX_PROP_VALUE);
                newSet.remove(subKey);
            }
            // or remove the old.
            else
                vertex.removeProperty(vertKey);
        }

        // Add the new entries.
        for (String item : newSet)
        {
            if (!(item instanceof String))
                throw new WindupException("Argument of " + method.getName() + " must be a Set<String>, but it contains: " + item.getClass());
            vertex.setProperty(prefix + item, "1");
        }
    }


    private void handleAdder(Vertex vertex, Method method, Object[] args, SetInProperties ann, FramedGraph<?> framedGraph)
    {
        if (args == null || args.length == 0)
            throw new WindupException("Method must take at least one String argument: " + method.getName());

        String prefix = preparePrefix(ann);

        for (Object arg : args)
        {
            if (!(arg instanceof String))
                throw new WindupException("The arguments of the add*() method " + method.getName() + " must be String, but was: " + arg.getClass());

            vertex.setProperty(prefix + arg, SET_VERTEX_PROP_VALUE);
        }
    }


    /**
     * Adder
     */
    private void handleAddAll(Vertex vertex, Method method, Object[] args, SetInProperties ann, FramedGraph<?> framedGraph)
    {
        if (args == null || args.length != 1)
            throw new WindupException("Method must take one String argument: " + method.getName());

        String prefix = preparePrefix(ann);

        // Argument.
        @SuppressWarnings("unchecked")
        Set<String> set = (Set<String>) args[0];

        // Store all set entries in vertex'es properties.
        for (String item : set)
        {
            vertex.setProperty(prefix + item, SET_VERTEX_PROP_VALUE);
        }
    }

    /**
     * Returns "&lt;ann.propertyPrefix()>&lt;SEPAR>", for example, "set:"; or an empty string if the prefix is empty.
     */
    private String preparePrefix(SetInProperties ann)
    {
        return "".equals(ann.propertyPrefix()) ? "" : (ann.propertyPrefix() + SetInProperties.SEPAR);
    }

}
