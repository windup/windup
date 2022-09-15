package org.jboss.windup.graph.model.resource;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;

/**
 * Indicates that a given file was ignored by windup.
 */
@TypeValue(IgnoredFileModel.TYPE)
public interface IgnoredFileModel extends FileModel {
    String TYPE = "IgnoredFileModel";
    String IGNORED_BY_REGEX = "IgnoredByRegex";

    /**
     * Contains the regex thanks to which this file was ignored.
     */
    @Property(IGNORED_BY_REGEX)
    String getIgnoredRegex();

    /**
     * Contains the regex thanks to which this file was ignored.
     */
    @Property(IGNORED_BY_REGEX)
    void setIgnoredRegex(String regex);
}
