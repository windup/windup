package org.jboss.windup.config.operation.foreach;

import com.thinkaurelius.titan.core.TitanGraph;
import com.tinkerpop.blueprints.Vertex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.script.ScriptException;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.For;
import org.jboss.windup.config.operation.IterationRoot;
import org.jboss.windup.config.operation.iteration.IterationPayloadManager;
import org.jboss.windup.config.operation.iteration.IterationSelectionManager;
import org.jboss.windup.config.selectables.VarStack;
import org.jboss.windup.graph.GraphUtil;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.util.exception.WindupException;


/**
 * Allows querying the graph using the Gremlin syntax.
 *
 * @author Ondrej Zizka, ozizka@redhat.com
 */
public class GremlinQueryImpl extends For implements GremlinQueryCriteria
{
    private final String gremlinQuery;
    private final IterationRoot root;
    private Map<String,Object> params;

    
    public GremlinQueryImpl(IterationRoot root, String gremlinQuery)
    {
        this.gremlinQuery = gremlinQuery;
        this.root = root;
    }


    /**
     * @returns A SelectionManager which performs a Gremlin query.
     */
    @Override
    public IterationSelectionManager getSelectionManager()
    {
        return new GremlinSelector();
    }



    private class GremlinSelector implements IterationSelectionManager
    {
        @Override
        public Iterable<WindupVertexFrame> getFrames(GraphRewrite event, VarStack varStack)
        {
            TitanGraph graph = event.getGraphContext().getGraph();
            final GremlinQuerier gq = new GremlinQuerier(graph);
            // Current syntax doesn't assume any pre-selected vertices, but in case we wanted that...
            //List<Vertex> initialVertices = getInitialVertices( event, varStack );
            //gq.setInitialVertices( initialVertices );
            
            Iterable<Vertex> resultVerts;
            try {
                resultVerts = gq.query( gremlinQuery, params, Vertex.class);
            } catch( ScriptException ex ) {
                throw new WindupException("Failed processing Gremling query :\n    "
                    + gremlinQuery + "\n    " + ex.getMessage(), ex);
            }
            return GraphUtil.toVertexFrames(event.getGraphContext(), resultVerts);
        }

        /**
         *  The initial vertices are those matched by previous query constructs.
         *  Iteration.[initial vertices].queryFor().[gremlin pipe wrappers]
         */
        private List<Vertex> getInitialVertices( GraphRewrite event, VarStack varStack ) {
            List<Vertex> initialVertices = new ArrayList<>();
            Iterable<WindupVertexFrame> initialFrames = root.getSelectionManager().getFrames(event, varStack);
            for (WindupVertexFrame frame : initialFrames)
                initialVertices.add(frame.asVertex());
            return initialVertices;
        }
    }


    // Params
    public Map<String, Object> getParams() {
        return params;
    }


    public void setParams( Map<String, Object> params ) {
        this.params = params;
    }

    
    
    // Unimplemented IterationRoot methods.
    @Override
    public IterationPayloadManager getPayloadManager() {
        throw new UnsupportedOperationException( "Not needed in GremlinQueryImpl." );
    }


    @Override
    public void setSelectionManager( IterationSelectionManager selManager ) {
        throw new UnsupportedOperationException( "Not needed in GremlinQueryImpl." );
    }


    @Override
    public void setPayloadManager( IterationPayloadManager payloadManager ) {
        throw new UnsupportedOperationException( "Not needed in GremlinQueryImpl." );
    }

    @Override
    public GremlinQueryCriteria endQuery() {
        return this;
    }
    
    
}
