package org.jboss.windup.reporting.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

@TypeValue("SourceReport")
public interface RuleModel extends WindupVertexFrame
{

    @Property("id")
    public int getId();
    
    @Property("id")
    public void setId( int id );
    
    
    @Adjacency(label = "provides", direction = Direction.IN)
    public RuleProviderModel getProvidedBy();

    @Adjacency(label = "provides", direction = Direction.IN)
    public void setProvidedBy(RuleProviderModel provider);
}
