package org.jboss.windup.graph;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;

/**
 * Contains various useful methods for dealing with Graph objects
 * 
 * @author jsightler
 * 
 */
public class GraphUtil
{
    /**
     * Adds the specified type to this frame, and returns a new object that implements this type.
     * 
     * @see GraphTypeManagerTest
     * 
     * @param frame
     * @param type
     * @return
     */
    public static <T extends WindupVertexFrame> T addTypeToModel(GraphContext graphContext, WindupVertexFrame frame,
                Class<T> type)
    {
        Vertex vertex = frame.asVertex();
        graphContext.getGraphTypeRegistry().addTypeToElement(type, vertex);
        graphContext.getGraph().commit();
        return graphContext.getFramed().frame(vertex, type);
    }

    public static List<WindupVertexFrame> toVertexFrames(GraphContext graphContext, Iterable<Vertex> vertices)
    {
        List<WindupVertexFrame> results = new ArrayList<>();
        for (Vertex v : vertices)
        {
            WindupVertexFrame frame = graphContext.getFramed().frame(v, WindupVertexFrame.class);
            results.add(frame);
        }
        return results;
    }
}
