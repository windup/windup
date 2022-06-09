package org.jboss.windup.rules.apps.diva.model;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.rules.apps.java.model.JavaMethodModel;

@TypeValue(DivaRestCallOpModel.TYPE)
public interface DivaRestCallOpModel extends DivaOpModel, DivaRestApiModel {
    String TYPE = "DivaRestCallOpModel";
    String CALL_PARAMS = "callParameters";
    String ENDPOINT = "endpoint";
    String ENDPOINT_METHOD = "endpointMethod";
    String ENDPOINT_CONTEXTS = "endpointContexts";

    @Adjacency(label = CALL_PARAMS, direction = Direction.OUT)
    List<DivaRequestParamModel> getCallParams();

    @Adjacency(label = CALL_PARAMS, direction = Direction.OUT)
    void addCallParam(DivaRequestParamModel param);

    @Adjacency(label = ENDPOINT, direction = Direction.OUT)
    DivaEndpointModel getEndpoint();

    @Adjacency(label = ENDPOINT, direction = Direction.OUT)
    void setEndpoint(DivaEndpointModel app);

    @Adjacency(label = ENDPOINT_METHOD, direction = Direction.OUT)
    JavaMethodModel getEndpointMethod();

    @Adjacency(label = ENDPOINT_METHOD, direction = Direction.OUT)
    void setEndpointMethod(JavaMethodModel m);

    @Adjacency(label = ENDPOINT_CONTEXTS, direction = Direction.OUT)
    List<DivaContextModel> getEndpointContexts();

    @Adjacency(label = ENDPOINT_CONTEXTS, direction = Direction.OUT)
    void addEndpointContext(DivaContextModel cxt);
}
