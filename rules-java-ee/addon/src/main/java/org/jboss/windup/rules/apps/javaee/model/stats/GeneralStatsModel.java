package org.jboss.windup.rules.apps.javaee.model.stats;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.Property;

import java.util.Date;
import java.util.List;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Model which maps the stats as adjacent vertices.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(GeneralStatsModel.TYPE)
public interface GeneralStatsModel extends WindupVertexFrame {
    String TYPE = "GeneralStatsModel";
    String COMPUTED = TYPE + "-computed";
    String ITEMS = TYPE + "-items";

    @Property(COMPUTED)
    Date getComputed();

    /**
     * The statistics items contained in this model.
     */
    @Adjacency(label = ITEMS, direction = Direction.OUT)
    List<GeneralStatsItemModel> getStatsItems();

    /**
     * The statistics items contained in this model.
     */
    @Adjacency(label = ITEMS, direction = Direction.OUT)
    GeneralStatsModel addStatsItem(GeneralStatsItemModel item);

}
