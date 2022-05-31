package org.jboss.windup.graph.typedgraph;

import com.syncleus.ferma.annotations.Incidence;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.List;

import static org.apache.tinkerpop.gremlin.structure.Direction.IN;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue("TestIncidenceBbbModel")
public interface TestIncidenceBbbModel extends WindupVertexFrame {
    @Property("prop1")
    String getProp1();

    @Property("prop1")
    TestIncidenceBbbModel setProp1(String prop);

    @Incidence(label = TestIncidenceAaaToBbbEdgeModel.TYPE, direction = IN)
    List<TestIncidenceAaaToBbbEdgeModel> getEdgesToAaa();
}
