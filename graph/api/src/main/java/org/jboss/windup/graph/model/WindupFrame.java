package org.jboss.windup.graph.model;

import com.syncleus.ferma.ElementFrame;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.jboss.windup.graph.DefaultValueInitializer;
import org.jboss.windup.graph.JavaHandler;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupFrame<T extends Element> extends ElementFrame
{
    /**
     * Name of the property where vertex/frame types are stored.
     *
     * @see org.jboss.windup.graph.GraphTypeManager
     */
    String TYPE_PROP = "w:winduptype";

    @JavaHandler(handler = Impl.class)
    void init ();

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

    class Impl {
        public void init(ElementFrame frame)
        {
            new DefaultValueInitializer().initalize(frame);
        }
    }
}
