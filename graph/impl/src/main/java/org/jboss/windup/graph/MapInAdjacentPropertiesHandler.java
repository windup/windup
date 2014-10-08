package org.jboss.windup.graph;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraph;
import com.tinkerpop.frames.modules.MethodHandler;

/**
 * Handles @MapInAdjacentProperties Map<String,String>.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class MapInAdjacentPropertiesHandler implements MethodHandler<MapInAdjacentProperties>
{
    private static final Logger log = Logging.get(MapInAdjacentPropertiesHandler.class);

    @Override
    public Class<MapInAdjacentProperties> getAnnotationType()
    {
        return MapInAdjacentProperties.class;
    }

    @Override
    public Map<String, Serializable> processElement(Object frame, Method method, Object[] arguments, MapInAdjacentProperties annotation,
                FramedGraph<?> framedGraph, Element elm)
    {
        if (!(elm instanceof Vertex))
            throw new WindupException("@" + MapInAdjacentProperties.class.getSimpleName() + " is only supported on Vertex objects.");

        String methodName = method.getName();
        if (methodName.startsWith("get"))
            return handleGetter((Vertex) elm, method, arguments, annotation, framedGraph);

        if (methodName.startsWith("set"))
        {
            handleSetter((Vertex) elm, method, arguments, annotation, framedGraph);
            return null;
        }

        throw new WindupException("Only get* and set* method names are supported for @" + MapInAdjacentProperties.class.getSimpleName());
    }

    /**
     * Getter
     */
    private Map<String, Serializable> handleGetter(Vertex vertex, Method method, Object[] args,
                MapInAdjacentProperties ann, FramedGraph<?> framedGraph)
    {
        if (args != null && args.length != 0)
            throw new WindupException("Method must take no arguments: " + method.getName());

        // Find the map vertex.
        Map<String, Serializable> map = new HashMap<>();
        Iterable<Vertex> verts = vertex.getVertices(Direction.OUT, ann.label());
        Vertex mapVertex = null;
        final Iterator<Vertex> it = verts.iterator();
        if (!it.hasNext())
        {
            // No map yet.
            return map;
        }
        else
        {
            mapVertex = it.next();
            if (it.hasNext())
            {
                // Multiple vertices behind edges with given label.
                log.warning("Found multiple vertices for a map, using only first one; for: " + method.getName());
            }
        }

        Set<String> keys = mapVertex.getPropertyKeys();
        for (String key : keys)
        {
            final Object val = mapVertex.getProperty(key);
            if (!(val instanceof String))
                log.warning("@InProperties is meant for Map<String,Serializable>, but the value was: " + val.getClass());
            map.put(key, "" + val);
        }
        return map;
    }

    /**
     * Setter
     */
    private void handleSetter(Vertex vertex, Method method, Object[] args, MapInAdjacentProperties ann,
                FramedGraph<?> framedGraph)
    {
        // Argument.
        if (args == null || args.length != 1)
            throw new WindupException("Method must take one argument: " + method.getName());

        if (!(args[0] instanceof Map))
            throw new WindupException("Argument of " + method.getName() + " must be a Map, but is: " + args[0].getClass());

        @SuppressWarnings("unchecked")
        Map<String, Serializable> map = (Map<String, Serializable>) args[0];

        // log.finer("Setting map under '"+ann.label()+"': " + StringUtils.join(map.keySet(), ", "));///

        // Find or create the map vertex.
        Iterable<Vertex> verts = vertex.getVertices(Direction.OUT, ann.label());
        Vertex mapVertex = null;
        final Iterator<Vertex> it = verts.iterator();
        if (!it.hasNext())
        {
            // No map vertex yet.
            // log.finest("No map vertex yet for: " + ann.label());///
            mapVertex = framedGraph.addVertex(null);
            vertex.addEdge(ann.label(), mapVertex);
        }
        else
        {
            mapVertex = it.next();
            if (it.hasNext())
            {
                // Multiple vertices behind edges with given label.
                log.warning("Found multiple vertices for a map, using only first one; for: " + method.getName());
            }
        }

        // For all keys in the old map...
        Set<String> keys = mapVertex.getPropertyKeys();
        Set<String> mapKeys = map.keySet();
        for (String key : keys)
        {
            final Object val = mapVertex.getProperty(key);
            if (!(val instanceof String))
            {
                log.warning("@InProperties is meant for Map<String,Serializable>, but the value was: " + val.getClass());
            }
            // ...either change to new value,
            if (map.containsKey(key))
            {
                mapVertex.setProperty(key, map.get(key));
                mapKeys.remove(key);
            }
            // or remove the old.
            else
                mapVertex.removeProperty(key);
        }

        // Add the new entries.
        for (String key : mapKeys)
        {
            // log.finest("Adding: " + key + " = " + map.get( key ));///
            mapVertex.setProperty(key, map.get(key));
        }
    }
}
