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
     * Name of the property where vertex/frame types are stored.
     * @see org.jboss.windup.graph.GraphTypeManager
     */
    public static final String TYPE_PROP = "type";

    
    @JavaHandler
    public String toPrettyString();

    
    abstract class Impl implements WindupVertexFrame, JavaHandlerContext<Vertex>
    {
        public String toPrettyString()
        {
            Vertex v = it();
            StringBuilder result = new StringBuilder();
            result.append("[").append(v.toString()).append("=");
            result.append("{");
            for (String propKey : v.getPropertyKeys())
            {
                Object propVal = v.getProperty(propKey);
                result.append(propKey).append(": ").append(propVal);
                result.append(", ");
            }
            result.deleteCharAt(result.length()-1);
            result.append("}]");
            return result.toString();
        }
    }

}
