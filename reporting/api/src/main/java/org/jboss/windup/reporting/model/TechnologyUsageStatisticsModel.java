package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Date;

/**
 * Maps particular set of statistic items known in advance in the properties of this single model.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @author <a href="mailto:jsightle@redhat.com">Jess Sightler</a>
 */
@TypeValue(TechnologyUsageStatisticsModel.TYPE)
public interface TechnologyUsageStatisticsModel extends TaggableModel {
    String TYPE = "TechnologyUsageStatisticsModel";

    String COMPUTED = "stats.computed";
    String PROJECT_MODEL = "stats.projectModel";

    String NAME = "stats.name";
    String OCCURRENCE_COUNT = "stats.occurrenceCount";


    /**
     * When this statistics were computed.
     */
    @Property(COMPUTED)
    Date getComputed();

    /**
     * When this statistics were computed.
     */
    @Property(COMPUTED)
    void setComputed(Date when);

    /**
     * ProjectModel for which the stats are computed.
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProjectModel();

    /**
     * ProjectModel for which the stats are computed.
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    TechnologyUsageStatisticsModel setProjectModel(ProjectModel projectModel);

    /**
     * Contains the name of the technology being counted.
     */
    @Property(NAME)
    @Indexed
    String getName();

    /**
     * Contains the name of the technology being counted.
     */
    @Property(NAME)
    void setName(String name);

    /**
     * Contains the number of occurrences.
     */
    @Property(OCCURRENCE_COUNT)
    int getOccurrenceCount();

    /**
     * Contains the number of occurrences.
     */
    @Property(OCCURRENCE_COUNT)
    void setOccurrenceCount(int count);
}
