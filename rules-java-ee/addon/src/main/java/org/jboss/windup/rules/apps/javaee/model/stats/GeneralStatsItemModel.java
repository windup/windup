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
    
    @Property(KEY) String getKey();
    @Property(KEY) GeneralStatsItemModel setKey(String key);
    
    @Property(LABEL) String getLabel();
    @Property(LABEL) GeneralStatsItemModel setLabel(String label);
    
    @Property(QUANTITY) int getQuantity();
    @Property(QUANTITY) GeneralStatsItemModel setQuantity(int qty);
}
