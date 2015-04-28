package org.jboss.windup.graph.model.resource;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates that a given file was ignored by windup.
 */
@TypeValue(IgnoredFileModel.TYPE)
public interface IgnoredFileModel extends ResourceModel
{
    public static final String TYPE = "IgnoredFileModel";
    public static final String IGNORED_BY_REGEX = "IgnoredByRegex";

    /**
     * Contains the regex thanks to which this file was ignored.
     */
    @Property(IGNORED_BY_REGEX)
    public String getIgnoredRegex();

    /**
     * Contains the regex thanks to which this file was ignored.
     */
    @Property(IGNORED_BY_REGEX)
    public void setIgnoredRegex(String regex);
}
