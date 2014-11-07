package org.jboss.windup.config.gremlinquery;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.Logging;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * 
 * Output the variable at the current point in the pipeline, and follow up to "depthToRecurse" levels of edges.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 *
 */
public class DebugStep implements GremlinTransform<Vertex, Vertex>
{
    private static Logger LOG = Logging.get(DebugStep.class);

    private int depthToRecurse;

    private DebugStep(int depthToRecurse)
    {
        this.depthToRecurse = depthToRecurse;
    }

    /**
     * Output the Vertices in the pipeline, with no recursion to related vertices
     */
    public static DebugStep output()
    {
        return new DebugStep(0);
    }

    /**
     * Output the Vertices in the pipeline, with the specified level of recursion to related edges
     */
    public static DebugStep output(int levels)
    {
        return new DebugStep(levels);
    }

    @Override
    public Vertex transform(GraphRewrite event, Vertex input)
    {
        // prevent loops of repeated information
        Set<Object> alreadySerialized = new HashSet<>();
        String serialized = serialize(alreadySerialized, input, 0);
        LOG.info(serialized);

        return input;
    }

    private void appendNTabs(StringBuilder sb, int tabCount)
    {
        for (int i = 0; i < tabCount; i++)
        {
            sb.append("\t");
        }
    }

    private String serialize(Set<Object> alreadySerialized, Vertex input, int level)
    {
        alreadySerialized.add(input.getId());

        StringBuilder sb = new StringBuilder();
        appendNTabs(sb, level);
        sb.append("VertexID: ");
        sb.append(input.getId().toString());
        sb.append("\n");
        appendNTabs(sb, level);
        sb.append("\tProperties: ");

        for (String propKey : input.getPropertyKeys())
        {
            Object propVal = input.getProperty(propKey);
            sb.append("\n\t\t");
            appendNTabs(sb, level);
            sb.append(propKey).append(": ").append(propVal);
        }

        if (level < depthToRecurse)
        {
            for (Edge edge : input.getEdges(Direction.OUT))
            {
                Vertex v = edge.getVertex(Direction.IN);
                if (alreadySerialized.contains(v.getId()))
                {
                    continue;
                }
                sb.append("\n");
                appendNTabs(sb, level);
                sb.append("\tOUT[").append(edge.getLabel()).append("]:\n");
                appendNTabs(sb, level);
                sb.append(serialize(alreadySerialized, v, level + 1));
            }
            for (Edge edge : input.getEdges(Direction.IN))
            {
                Vertex v = edge.getVertex(Direction.OUT);
                if (alreadySerialized.contains(v.getId()))
                {
                    continue;
                }
                sb.append("\n");
                appendNTabs(sb, level);
                sb.append("\tIN[").append(edge.getLabel()).append("]:\n");
                appendNTabs(sb, level);
                sb.append(serialize(alreadySerialized, v, level + 1));
            }
        }
        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "Debug.output(" + this.depthToRecurse + ")";
    }
}
