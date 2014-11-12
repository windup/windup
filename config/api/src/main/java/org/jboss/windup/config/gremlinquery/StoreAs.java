package org.jboss.windup.config.gremlinquery;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.sideeffect.AggregatePipe;

/**
 * A {@link GremlinStep} that simply stores the current items into {@link Variables} with the given name.
 */
public class StoreAs implements GremlinQueryFilter<Vertex, Vertex>
{
    private String var;

    public StoreAs(String var)
    {
        this.var = var;
    }

    @Override
    public GremlinPipeline<Vertex, Vertex> process(GraphRewrite event, GremlinPipeline<Vertex, Vertex> pipe)
    {
        final List<Vertex> list = new ArrayList<>();
        final FramedVertexIterable<WindupVertexFrame> framedIterable = new FramedVertexIterable<>(event.getGraphContext().getFramed(), list,
                    WindupVertexFrame.class);
        Variables.instance(event).setVariable(this.var, framedIterable);
        return pipe.add(new AggregatePipe<Vertex>(list)
        {
            @Override
            protected Vertex processNextStart()
            {
                Vertex v = super.processNextStart();
                return v;
            }
        });
    }

    @Override
    public String toString()
    {
        return "storeAs(" + this.var + ")";
    }
}
