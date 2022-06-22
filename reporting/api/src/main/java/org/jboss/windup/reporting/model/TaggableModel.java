package org.jboss.windup.reporting.model;

import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.Adjacency;
import org.jboss.windup.reporting.TagUtil;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TaggableModel.TYPE)
public interface TaggableModel extends WindupVertexFrame {
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
    TagSetModel getTagModelNotNullSafe();

    default TagSetModel getTagModel() {
        try {
            return getTagModelNotNullSafe();
        } catch (NoSuchElementException e) {
            return null;
        }
    }

    /**
     * Gets the {@link Set} of tags associated with this vertex.
     */
    default Set<String> getTags() {
        TagSetModel tagSetModel = getTagModel();
        if (tagSetModel == null)
            return Collections.emptySet();
        return tagSetModel.getTags();
    }

    /**
     * Returns true if this {@link TaggableModel} matches the provided inclusion and exclusion tags.
     * <p>
     * {@see TagUtil}
     */
    default boolean matchesTags(Set<String> includeTags, Set<String> excludeTags) {
        return TagUtil.checkMatchingTags(this.getTags(), includeTags, excludeTags);
    }
}
