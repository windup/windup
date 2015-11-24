package org.jboss.windup.reporting.model;

import java.util.Collections;
import java.util.Set;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TaggableModel.TYPE)
public interface TaggableModel extends WindupVertexFrame
{
    String TYPE = "TaggableModel";
    String TAG = "tag";

    /**
     * Set the set of tags associated with this {@link ClassificationModel}
     */
    @Adjacency(label = TAG, direction = Direction.OUT)
    void setTagModel(TagSetModel tags);

    /**
     * Get the set of tags associated with this {@link ClassificationModel}
     */
    @Adjacency(label = TAG, direction = Direction.OUT)
    TagSetModel getTagModel();

    @JavaHandler
    Set<String> getTags();

    abstract class Impl implements TaggableModel, JavaHandlerContext<Vertex>
    {
        @Override
        public Set<String> getTags()
        {
            TagSetModel tagSetModel = getTagModel();
            if (tagSetModel == null)
                return Collections.emptySet();
            return tagSetModel.getTags();
        }
    }
}
