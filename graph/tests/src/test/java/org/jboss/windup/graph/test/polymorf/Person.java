package org.jboss.windup.graph.test.polymorf;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.VertexFrame;
import com.tinkerpop.frames.modules.typedgraph.TypeField;

/**
 * 
 * @author Ondrej Zizka, ozizka at redhat.com
 */
@TypeField(WindupVertexFrame.TYPE_PROP)
public interface Person extends VertexFrame
{
    @Property("name")
    public String getName();

    @Adjacency(label = "parentOf")
    public Iterable<Person> getParents();

    @Adjacency(label = "parentOf")
    public void addPerson(final Person person);
}
