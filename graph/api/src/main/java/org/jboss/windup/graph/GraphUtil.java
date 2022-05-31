package org.jboss.windup.graph;

import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
public class GraphUtil {
    /**
     * Formats a vertex using it's properties. Debugging purposes.
     */
    public static final String vertexAsString(Vertex vertex, int depth, String withEdgesOfLabel) {
        StringBuilder sb = new StringBuilder();
        vertexAsString(vertex, depth, withEdgesOfLabel, sb, 0, new HashSet<>());
        return sb.toString();
    }

    private static final void vertexAsString(Vertex vertex, int depth, String withEdgesOfLabel, StringBuilder sb, int atLevel, Set<Object> visitedIDs) {
        String indent = StringUtils.repeat("    ", atLevel);
        if (vertex == null) {
            sb.append(System.lineSeparator()).append(indent).append("(vertex == null)");
            return;
        }
        if (visitedIDs.contains(vertex.id())) {
            sb.append(System.lineSeparator()).append(indent).append("" + vertex.id());
            return;
        }

        visitedIDs.add(vertex.id());

        sb.append(System.lineSeparator()).append(indent).append("v #").append("" + vertex.id()).append(" {");
        boolean hasProps = !vertex.keys().isEmpty();
        boolean hasEdges = vertex.edges(Direction.IN).hasNext()
                || vertex.edges(Direction.OUT).hasNext();

        for (String propKey : vertex.keys()) {
            sb.append(System.lineSeparator()).append(indent).append(propKey).append(": ").append("" + vertex.property(propKey));
        }

        if (withEdgesOfLabel == null || depth == 0) {
            if (hasProps)
                sb.append(System.lineSeparator()).append(indent);
            if (hasEdges)
                sb.append("... + some edges...");
        } else {
            boolean allEdges = "*".equals(withEdgesOfLabel);
            sb.append(System.lineSeparator()).append(indent).append(withEdgesOfLabel).append(" OUT -> ");

            Iterator<Edge> edgesOutIterator = allEdges ? vertex.edges(Direction.OUT) : vertex.edges(Direction.OUT, withEdgesOfLabel);
            while (edgesOutIterator.hasNext()) {
                Edge edge = edgesOutIterator.next();
                if (allEdges)
                    sb.append(System.lineSeparator()).append(indent).append(edge.label()).append(" --> ");
                vertexAsString(edge.inVertex(), depth - 1, withEdgesOfLabel, sb, atLevel + 1, visitedIDs);
            }
            sb.append(System.lineSeparator()).append(indent).append(withEdgesOfLabel).append(" <- IN");

            Iterator<Edge> edgesInIterator = allEdges ? vertex.edges(Direction.IN) : vertex.edges(Direction.IN, withEdgesOfLabel);
            while (edgesInIterator.hasNext()) {
                Edge edge = edgesInIterator.next();
                if (allEdges)
                    sb.append(System.lineSeparator()).append(indent).append(" <-- ").append(edge.label()).append(" --> ");
                vertexAsString(edge.outVertex(), depth - 1, withEdgesOfLabel, sb, atLevel + 1, visitedIDs);
            }
        }

        if (hasProps || hasEdges)
            sb.append('\n').append(indent);
        sb.append("}\n");
    }
}
