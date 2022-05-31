package org.jboss.windup.graph.typedgraph;

import com.syncleus.ferma.annotations.InVertex;
import com.syncleus.ferma.annotations.OutVertex;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupEdgeFrame;

import static org.jboss.windup.graph.typedgraph.TestIncidenceAaaToBbbEdgeModel.TYPE;

/**
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue(TYPE)
public interface TestIncidenceAaaToBbbEdgeModel extends WindupEdgeFrame {
    static final String TYPE = "TestIncidenceAaaToBbbEdge";

    @Property("prop1")
    String getProp1();

    @Property("prop1")
    TestIncidenceAaaToBbbEdgeModel setProp1(String prop);

    @OutVertex
    TestIncidenceAaaModel getAaa();

    @InVertex
    TestIncidenceBbbModel getBbb();
}
