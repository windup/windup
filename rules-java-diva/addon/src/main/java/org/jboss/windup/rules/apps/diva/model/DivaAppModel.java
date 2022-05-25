package org.jboss.windup.rules.apps.diva.model;

import java.util.List;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;

@TypeValue(DivaAppModel.TYPE)
public interface DivaAppModel extends ProjectModel, DivaEndpointModel {

    String TYPE = "DivaAppModel";
    String DATASOURCE = "datasource"; // TODO: refer to datasource properties
    String CONTEXTS = "contexts";

    @Property(DATASOURCE)
    String getDatasource();

    @Property(DATASOURCE)
    void setDatasource(String datasource);

    @Adjacency(label = CONTEXTS, direction = Direction.OUT)
    List<DivaContextModel> getContexts();

    @Adjacency(label = CONTEXTS, direction = Direction.OUT)
    void addContext(DivaContextModel contexts);

}
