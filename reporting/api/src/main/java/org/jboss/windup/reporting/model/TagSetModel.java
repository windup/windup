package org.jboss.windup.reporting.model;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

import java.util.Set;

/**
 * Represents that a model has tags, and also contains the methods for accessing those tags.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(TagSetModel.TYPE)
public interface TagSetModel extends WindupVertexFrame {
    String TYPE = "TagSetModel";
    String PREFIX = "TAGS";

    /**
     * Gets the tags associated with this {@link Vertex}.
     */
    @SetInProperties(propertyPrefix = PREFIX)
    Set<String> getTags();

    /**
     * Sets the tags associated with this {@link Vertex}.
     */
    @SetInProperties(propertyPrefix = PREFIX)
    TagSetModel setTags(Set<String> tags);
}
