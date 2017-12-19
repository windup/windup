package org.jboss.windup.graph.model;

import com.syncleus.ferma.VertexFrame;
import org.apache.tinkerpop.gremlin.structure.Element;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupFrame<T extends Element> extends VertexFrame
{
    /**
     * Name of the property where vertex/frame types are stored.
     *
     * @see org.jboss.windup.graph.GraphTypeManager
     */
    String TYPE_PROP = "w:winduptype";

    /**
     * A string representation of this vertex, showing it's properties in a JSON-like format.
     */
    default String toPrettyString()
    {
        Element v = getElement();
        StringBuilder result = new StringBuilder();
        result.append("[").append(v.toString()).append("=");
        result.append("{");

        boolean hasSome = false;
        for (String propKey : v.keys())
        {
            hasSome = true;
            Object propVal = v.property(propKey);
            result.append(propKey).append(": ").append(propVal);
            result.append(", ");
        }

        if (hasSome)
        {
            result.delete(result.length() - 2, result.length());
        }

        result.append("}]");
        return result.toString();
    }
}
