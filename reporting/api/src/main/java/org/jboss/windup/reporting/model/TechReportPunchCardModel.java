package org.jboss.windup.reporting.model;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.config.tags.Tag;

/**
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@TypeValue(TechReportPunchCardModel.TYPE)
public interface TechReportPunchCardModel extends ApplicationReportModel, IncludeAndExcludeTagsModel
{
    String TYPE = "TechReportPunchCardModel";
    String TAG_NAME_SECTORS = "techreport-sectors:";


    /**
     * This tag contains tags that serve as sector groups/headers in the report.
     * And those in turn contain the technologies.
     */
    @Adjacency(label = TAG_NAME_SECTORS, direction = Direction.OUT)
    TagModel getSectorsHolderTag();

    @Adjacency(label = TAG_NAME_SECTORS, direction = Direction.OUT)
    void setSectorsHolderTag(TagModel tag);
}
