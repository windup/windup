package org.jboss.windup.graph;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GraphUtil
{
    /**
     * Formats a vertex using it's properties. Debugging purposes.
     */
    public static final String vertexAsString(Vertex vertex, int depth, String withEdgesOfLabel)
    {
        StringBuilder sb = new StringBuilder();
        vertexAsString(vertex, depth, withEdgesOfLabel, sb, 0, new HashSet<>());
        return sb.toString();
    }

    private static final void vertexAsString(Vertex vertex, int depth, String withEdgesOfLabel, StringBuilder sb, int atLevel, Set<Object> visitedIDs)
    {
        String indent = StringUtils.repeat("    ", atLevel);
        if (vertex == null)
        {
            sb.append(System.lineSeparator()).append(indent).append("(vertex == null)");
            return;
        }
        if (visitedIDs.contains(vertex.getId()))
        {
            sb.append(System.lineSeparator()).append(indent).append("" + vertex.getId());
            return;
        }

        visitedIDs.add(vertex.getId());

        sb.append(System.lineSeparator()).append(indent).append("v #").append("" + vertex.getId()).append(" {");
        boolean hasProps = !vertex.getPropertyKeys().isEmpty();
        boolean hasEdges = vertex.getEdges(Direction.IN).iterator().hasNext()
                    || vertex.getEdges(Direction.OUT).iterator().hasNext();

        for (String propKey : vertex.getPropertyKeys())
        {
            sb.append(System.lineSeparator()).append(indent).append(propKey).append(": ").append("" + vertex.getProperty(propKey));
        }

        if (withEdgesOfLabel == null || depth == 0)
        {
            if (hasProps)
                sb.append(System.lineSeparator()).append(indent);
            if (hasEdges)
                sb.append("... + some edges...");
        }
        else
        {
            boolean allEdges = "*".equals(withEdgesOfLabel);
            sb.append(System.lineSeparator()).append(indent).append(withEdgesOfLabel).append(" OUT -> ");
            for (Edge edge : allEdges ? vertex.getEdges(Direction.OUT) : vertex.getEdges(Direction.OUT, withEdgesOfLabel))
            {
                if (allEdges)
                    sb.append(System.lineSeparator()).append(indent).append(edge.getLabel()).append(" --> ");
                vertexAsString(edge.getVertex(Direction.IN), depth - 1, withEdgesOfLabel, sb, atLevel + 1, visitedIDs);
            }
            sb.append(System.lineSeparator()).append(indent).append(withEdgesOfLabel).append(" <- IN");
            for (Edge edge : allEdges ? vertex.getEdges(Direction.IN) : vertex.getEdges(Direction.IN, withEdgesOfLabel))
            {
                if (allEdges)
                    sb.append(System.lineSeparator()).append(indent).append(" <-- ").append(edge.getLabel()).append(" --> ");
                vertexAsString(edge.getVertex(Direction.OUT), depth - 1, withEdgesOfLabel, sb, atLevel + 1, visitedIDs);
            }
        }

        if (hasProps || hasEdges)
            sb.append('\n').append(indent);
        sb.append("}\n");
    }
}
