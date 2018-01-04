package org.jboss.windup.graph.typedgraph;

import static com.tinkerpop.blueprints.Direction.OUT;
import com.tinkerpop.frames.Incidence;

import com.syncleus.ferma.annotations.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

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
