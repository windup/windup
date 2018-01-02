package org.jboss.windup.graph.typedgraph;


import com.syncleus.ferma.annotations.Incidence;
import com.syncleus.ferma.annotations.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import static org.apache.tinkerpop.gremlin.structure.Direction.OUT;

/**
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("TestIncidenceAaa")
public interface TestIncidenceAaaModel extends WindupVertexFrame
{
    @Property("prop1")
    TestIncidenceAaaModel setProp1(String prop);

    @Property("prop1")
    String getProp1();

    @Incidence(label = TestIncidenceAaaToBbbEdgeModel.TYPE, direction = OUT)
    Iterable<TestIncidenceAaaToBbbEdgeModel> getEdgesToBbb();
}
