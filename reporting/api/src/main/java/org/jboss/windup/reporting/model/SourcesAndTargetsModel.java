package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.model.TechnologyReferenceModel;

import java.util.List;

public interface SourcesAndTargetsModel {
    String SOURCE_TECHNOLOGIES = "sourceTechnologies";
    String TARGET_TECHNOLOGIES = "targetTechnologies";

    @Adjacency(label = SOURCE_TECHNOLOGIES, direction = Direction.OUT)
    List<TechnologyReferenceModel> getSourceTechnologies();

    @Adjacency(label = SOURCE_TECHNOLOGIES, direction = Direction.OUT)
    void setSourceTechnologies(List<TechnologyReferenceModel> sourceTechnologies);

    @Adjacency(label = TARGET_TECHNOLOGIES, direction = Direction.OUT)
    List<TechnologyReferenceModel> getTargetTechnologies();

    @Adjacency(label = TARGET_TECHNOLOGIES, direction = Direction.OUT)
    void setTargetTechnologies(List<TechnologyReferenceModel> targetTechnologies);
}
