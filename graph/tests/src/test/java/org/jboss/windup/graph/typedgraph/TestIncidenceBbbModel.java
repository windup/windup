package org.jboss.windup.graph.typedgraph;

import static com.tinkerpop.blueprints.Direction.IN;
import com.tinkerpop.frames.Incidence;

import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("TestIncidenceBbbModel")
public interface TestIncidenceBbbModel extends WindupVertexFrame
{
    @Property("prop1")
    TestIncidenceBbbModel setProp1(String prop);

    @Property("prop1")
    String getProp1();

    @Incidence(label = TestIncidenceAaaToBbbEdgeModel.TYPE, direction = IN)
    Iterable<TestIncidenceAaaToBbbEdgeModel> getEdgesToAaa();
}
