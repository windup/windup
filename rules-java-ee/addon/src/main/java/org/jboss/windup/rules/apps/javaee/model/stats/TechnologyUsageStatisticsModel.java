package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

import java.util.Date;

import org.jboss.windup.graph.Indexed;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.reporting.model.TaggableModel;

/**
 * Maps particular set of statistic items known in advance in the properties of this single model.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 * @author <a href="mailto:jsightle@redhat.com">Jess Sightler</a>
 */
@TypeValue(TechnologyUsageStatisticsModel.TYPE)
public interface TechnologyUsageStatisticsModel extends TaggableModel
{
    String TYPE = "TechnologyUsageStatisticsModel";

    String COMPUTED = TYPE + "_computed";
    String PROJECT_MODEL = "stats.projectModel";

    String NAME = "stats.name";
    String OCCURRENCE_COUNT = "stats.occurrencecount";


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
     * ProjectModel for computed stats
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProjectModel();

    /**
     * ProjectModel for computed stats
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
