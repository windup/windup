package org.jboss.windup.reporting.service;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.pipes.PipeFunction;
import java.util.Set;

/**
 *  Filters the pipeline based upon a {@link Set} of vertices.
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public final class SetMembersFilter implements PipeFunction<Vertex, Boolean>
{
    private final Set<Vertex> initialVertices;

    /**
     * Only items that match the provided set of vertices will be passed through the pipeline.
     */
    public SetMembersFilter(Set<Vertex> initialVertices)
    {
        this.initialVertices = initialVertices;
    }

    @Override
    public Boolean compute(Vertex argument)
    {
        return initialVertices.contains(argument);
    }
}
