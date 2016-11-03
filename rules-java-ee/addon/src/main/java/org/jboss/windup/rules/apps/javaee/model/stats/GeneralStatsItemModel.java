package org.jboss.windup.rules.apps.javaee.model.stats;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * A single stats item.
 */
@TypeValue(GeneralStatsItemModel.TYPE)
public interface GeneralStatsItemModel extends WindupVertexFrame
{
    String TYPE = "GeneralStatsItem";
    String KEY      = TYPE + "_key";
    String LABEL    = TYPE + "_label";
    String QUANTITY = TYPE + "_qty";

    /**
     * A key of the statistics item, under which it is accessible independently of the particular report model.
     */
    @Property(KEY) String getKey();

    /**
     * A key of the statistics item, under which it is accessible independently of the particular report model.
     */
    @Property(KEY) GeneralStatsItemModel setKey(String key);

    /**
     * The label for this statistic to be used when displayed.
     */
    @Property(LABEL) String getLabel();

    /**
     * The label for this statistic to be used when displayed.
     */
    @Property(LABEL) GeneralStatsItemModel setLabel(String label);

    /**
     * The quantity of the observed statistic.
     */
    @Property(QUANTITY) int getQuantity();

    /**
     * The quantity of the observed statistic.
     */
    @Property(QUANTITY) GeneralStatsItemModel setQuantity(int qty);
}
