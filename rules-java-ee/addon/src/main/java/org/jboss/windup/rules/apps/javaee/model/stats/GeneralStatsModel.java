package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import java.util.Date;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Model which maps the stats as adjacent vertices.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(GeneralStatsModel.TYPE)
public interface GeneralStatsModel extends WindupVertexFrame
{
    String TYPE = "GeneralStatsModel";
    String COMPUTED = TYPE + "_computed";
    String ITEMS = TYPE + "_items";

    @Property(COMPUTED)
    Date getComputed();

    /**
     * The statistics items contained in this model.
     */
    @Adjacency(label = ITEMS, direction = Direction.OUT)
    Iterable<GeneralStatsItemModel> getStatsItems();

    /**
     * The statistics items contained in this model.
     */
    @Adjacency(label = ITEMS, direction = Direction.OUT)
    GeneralStatsModel addStatsItem(GeneralStatsItemModel item);

}
