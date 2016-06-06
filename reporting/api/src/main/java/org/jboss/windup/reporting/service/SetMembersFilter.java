package org.jboss.windup.reporting.service;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;
import java.util.Set;

/**
 *
 *  @author <a href="http://ondra.zizka.cz/">Ondrej Zizka, zizka@seznam.cz</a>
 */
public final class SetMembersFilter implements PipeFunction<Vertex, Boolean>
{
    private final Set<Vertex> initialVertices;


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
