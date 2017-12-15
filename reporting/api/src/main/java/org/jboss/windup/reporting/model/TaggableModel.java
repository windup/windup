package org.jboss.windup.reporting.model;

import java.util.Collections;
import java.util.Set;

import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;
import org.jboss.windup.reporting.TagUtil;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TaggableModel.TYPE)
public interface TaggableModel extends WindupVertexFrame
{
    /**
     * This location for this tag is not ideal. TODO - Find a better place for this...
     */
    String CATCHALL_TAG = "catchall";

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

    /**
     * Gets the {@link Set} of tags associated with this vertex.
     */
    @JavaHandler
    Set<String> getTags();

    /**
     * Returns true if this {@link TaggableModel} matches the provided inclusion and exclusion tags.
     *
     * {@see TagUtil}
     */
    @JavaHandler
    boolean matchesTags(Set<String> includeTags, Set<String> excludeTags);


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

        public boolean matchesTags(Set<String> includeTags, Set<String> excludeTags)
        {
            return TagUtil.checkMatchingTags(this.getTags(), includeTags, excludeTags);
        }
    }
}
