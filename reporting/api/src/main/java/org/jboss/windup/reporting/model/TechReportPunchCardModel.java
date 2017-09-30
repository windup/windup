package org.jboss.windup.reporting.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.graph.MapInAdjacentProperties;

import java.util.Map;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(TechReportPunchCardModel.TYPE)
public interface TechReportPunchCardModel extends ApplicationReportModel, IncludeAndExcludeTagsModel
{
    String TYPE = "TechReportPunchCardModel";
    public String EDGE_TAG_SECTORS = "techreport-sectors:"; // Also a tag name.
    public String EDGE_TAG_ROWS = "techreport-rows:";       // Also a tag name.


    /**
     * This tag contains tags that serve as sector groups/headers in the report.
     * And those in turn contain the technologies.
     */
    @Adjacency(label = EDGE_TAG_SECTORS, direction = Direction.OUT)
    TagModel getSectorsHolderTag();

    @Adjacency(label = EDGE_TAG_SECTORS, direction = Direction.OUT)
    void setSectorsHolderTag(TagModel tag);

    @Adjacency(label = EDGE_TAG_ROWS, direction = Direction.OUT)
    TagModel getRowsHolderTag();

    @Adjacency(label = EDGE_TAG_ROWS, direction = Direction.OUT)
    void setRowsHolderTag(TagModel tag);


    @MapInAdjacentProperties(label = "maxCounts")
    Map<String, Integer> getMaximumCounts();
    @MapInAdjacentProperties(label = "maxCounts")
    TagModel setMaximumCounts(Map<String, Integer> maxCounts);
}
