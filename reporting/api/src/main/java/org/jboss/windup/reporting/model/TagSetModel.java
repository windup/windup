package org.jboss.windup.reporting.model;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.WindupVertexFrame;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents that a model has tags, and also contains the methods for accessing those tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TagSetModel.TYPE)
public interface TagSetModel extends WindupVertexFrame
{
    String TYPE = "TagSetModel";
    String PREFIX = "TAGS";

    /**
     * Sets the tags associated with this {@link Vertex}.
     */
    @SetInProperties(propertyPrefix = PREFIX)
    TagSetModel setTags(Set<String> tags);

    /**
     * Gets the tags associated with this {@link Vertex}.
     */
    @SetInProperties(propertyPrefix = PREFIX)
    Set<String> getTags();
}
