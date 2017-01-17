package org.jboss.windup.rules.apps.javaee.model.stats;

import java.util.Date;

import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:dklingenberg@gmail.com">David Klingenberg</a>
 */
@TypeValue(ProjectTechnologiesStatsModel.TYPE)
public interface ProjectTechnologiesStatsModel extends WindupVertexFrame
{
    String TYPE = "ProjectTechnologiesStats";
    String COMPUTED = TYPE + "_computed";

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

    String PROJECT_MODEL = "stats.projectModel";

    /**
     * ProjectModel for computed stats
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    ProjectModel getProjectModel();

    /**
     * ProjectModel for computed stats
     */
    @Adjacency(label = PROJECT_MODEL, direction = Direction.OUT)
    ProjectTechnologiesStatsModel setProjectModel(ProjectModel projectModel);

    String TECHNOLOGIES_STATS_MODEL = "stats.technologiesStatsModel";

    /**
     * Contains the link to the stats data.
     */
    @Adjacency(label = TECHNOLOGIES_STATS_MODEL, direction = Direction.OUT)
    TechnologiesStatsModel getTechnologiesStatsModel();

    /**
     * Contains the link to the stats data.
     */
    @Adjacency(label = TECHNOLOGIES_STATS_MODEL, direction = Direction.OUT)
    ProjectTechnologiesStatsModel setTechnologiesStatsModel(TechnologiesStatsModel technologiesStatsModel);
}
