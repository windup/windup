package org.jboss.windup.graph.typedgraph;

import com.tinkerpop.frames.InVertex;
import com.tinkerpop.frames.OutVertex;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupEdgeFrame;
import static org.jboss.windup.graph.typedgraph.TestIncidenceAaaToBbbEdgeModel.TYPE;

/**
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@TypeValue(TYPE)
public interface TestIncidenceAaaToBbbEdgeModel extends WindupEdgeFrame
{
    static final String TYPE = "TestIncidenceAaaToBbbEdge";

    @Property("prop1")
    TestIncidenceAaaToBbbEdgeModel setProp1(String prop);

    @Property("prop1")
    String getProp1();

    @OutVertex
    TestIncidenceAaaModel getAaa();

    @InVertex
    TestIncidenceBbbModel getBbb();
}