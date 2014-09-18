package org.jboss.windup.reporting.service;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;

import com.thinkaurelius.titan.core.attribute.Text;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.FramedGraphQuery;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import com.tinkerpop.gremlin.java.GremlinPipeline;

/**
 * Contains methods for finding, creating, and deleting {@link TechnologyTagModel} instances.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class TechnologyTagService extends GraphService<TechnologyTagModel>
{

    public TechnologyTagService(GraphContext context)
    {
        super(context, TechnologyTagModel.class);
    }

    /**
     * Adds the provided tag to the provided {@link FileModel}. If a {@link TechnologyTagModel} cannot be found with the
     * provided name, then one will be created.
     */
    public TechnologyTagModel addTagToFileModel(FileModel fileModel, String tagName, TechnologyTagLevel level)
    {
        FramedGraphQuery q = getGraphContext().getQuery().type(TechnologyTagModel.class)
                    .has(TechnologyTagModel.NAME, tagName);
        TechnologyTagModel m = super.getUnique(q);
        if (m == null)
        {
            m = create();
            m.setName(tagName);
            m.setLevel(level);
        }
        m.addFileModel(fileModel);
        return m;
    }

    /**
     * Return an {@link Iterable} containing all {@link TechnologyTagModel}s that are directly associated with the
     * provided {@link FileModel}.
     */
    public Iterable<TechnologyTagModel> findTechnologyTagsForFile(FileModel fm)
    {
        GremlinPipeline<Vertex, Vertex> pipeline = new GremlinPipeline<>(fm.asVertex());
        pipeline.in(TechnologyTagModel.TECH_TAG_TO_FILE_MODEL)
                    .has(WindupVertexFrame.TYPE_PROP, Text.CONTAINS, TechnologyTagModel.TYPE);
        return new FramedVertexIterable<TechnologyTagModel>(getGraphContext().getFramed(), pipeline,
                    TechnologyTagModel.class);
    }
}
