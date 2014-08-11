package org.jboss.windup.graph.model;

import org.jboss.windup.graph.service.GraphService;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

/**
 * The base {@link VertexFrame} type implemented by all model types.
 */
@TypeField(WindupVertexFrame.TYPE_PROP)
public interface WindupVertexFrame extends VertexFrame
{
    /**
     * Stores the vertex types for graph lookup via {@link GraphService} and other query mechanisms.
     */
    public static final String TYPE_PROP = "w:vertextype";



    @JavaHandler
    public String toPrettyString();

    abstract class Impl implements WindupVertexFrame, JavaHandlerContext<Vertex>
    {
        public String toPrettyString()
        {
            return WindupVertexFrame.Impl.toPrettyString(it());
        }
        
        public static String toPrettyString( Vertex v )
        {
            StringBuilder result = new StringBuilder();
            result.append("[").append(v.toString()).append("=");
            result.append("{");
            boolean first = true;
            for (String propKey : v.getPropertyKeys())
            {
                if (first)
                {
                    first = false;
                }
                else
                {
                    result.append(", ");
                }
                Object propVal = v.getProperty(propKey);
                result.append(propKey).append(": ").append(propVal);
            }
            result.append("}");
            result.append("]");
            return result.toString();
        }
    }

}
