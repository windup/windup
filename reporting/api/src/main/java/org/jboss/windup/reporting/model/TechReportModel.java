package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.graph.MapInAdjacentVertices;
import org.jboss.windup.graph.model.TypeValue;

import java.util.Map;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(TechReportModel.TYPE)
public interface TechReportModel extends ApplicationReportModel, IncludeAndExcludeTagsModel {
    String TYPE = "TechReportModel";
    String EDGE_TAG_SECTORS = "techreport-sectors:"; // Also a tag name.
    String EDGE_TAG_ROWS = "techreport-rows:";       // Also a tag name.
    String EDGE_FOR_APP = "forApplication";


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

    @MapInAdjacentVertices(label = "appProjectIdToReportMap")
    Map<String, TechReportModel> getAppProjectIdToReportMap();

    @MapInAdjacentVertices(label = "appProjectIdToReportMap")
    void setAppProjectIdToReportMap(Map<String, TechReportModel> values);


    /*
    @MapInAdjacentProperties(label = "maxCounts")
    Map<String, Integer> getMaximumCounts();
    @MapInAdjacentProperties(label = "maxCounts")
    TagModel setMaximumCounts(Map<String, Integer> maxCounts);
    */
}
