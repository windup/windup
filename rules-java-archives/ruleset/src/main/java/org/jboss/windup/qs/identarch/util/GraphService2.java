package org.jboss.windup.qs.identarch.util;


import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.util.exception.WindupException;

/**
 * TODO: Merge to GraphService in Windup core.
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public class GraphService2<T extends WindupVertexFrame> //extends org.jboss.windup.graph.service.GraphService2<T>
{

    private Class<T> type;
    private GraphContext grCtx;

    public GraphService2(GraphContext context, Class<T> type)
    {
        this.grCtx = context;
        this.type = type;
    }


    /**
    * Takes an object implementing a model interface,
    * creates a vertex and copies the properties into the returned proxy.
    * Something like JPA's merge().
    * This method doesn't deal with any ID's; maybe in the future.
    * So, the vertex is always created and returned.
    */
    public T merge(T source)
    {
        T frame = this.grCtx.getFramed().addVertex(null, this.type);
        try
        {
            PropertyUtils.copyProperties(source, frame);
        }
        catch( Exception ex )
        {
            throw new WindupException("Failed copying properties into frame from: "
                + frame.getClass().getName() + "\n " + ex.getMessage(), ex
            );
        }

        return frame;
    }

    public T reload(T source)
    {
        return new GraphService<T>(grCtx, type).getById(source.asVertex().getId());
    }

}// class
