package org.jboss.windup.graph.model;

import org.apache.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface WindupFrame<T extends Element>
{
    /**
     * Name of the property where vertex/frame types are stored.
     *
     * @see org.jboss.windup.graph.GraphTypeManager
     */
    String TYPE_PROP = "w:winduptype";

    @JavaHandler
    @Override
    String toString();

    /**
     * A string representation of this vertex, showing it's properties in a JSON-like format.
     */
    @JavaHandler
    String toPrettyString();

    abstract class Impl<T extends Element> implements WindupVertexFrame, JavaHandlerContext<T>
    {
        @Override
        public String toString()
        {
            return toPrettyString();
        }

        public String toPrettyString()
        {
            Element v = it();
            StringBuilder result = new StringBuilder();
            result.append("[").append(v.toString()).append("=");
            result.append("{");

            boolean hasSome = false;
            for (String propKey : v.getPropertyKeys())
            {
                hasSome = true;
                Object propVal = v.getProperty(propKey);
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
}
