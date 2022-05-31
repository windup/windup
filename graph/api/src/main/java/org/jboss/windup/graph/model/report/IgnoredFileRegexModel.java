package org.jboss.windup.graph.model.report;

import org.jboss.windup.graph.Property;
import org.jboss.windup.graph.model.TypeValue;
import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Indicates which files within archives should be skipped by Windup.
 */
@TypeValue(IgnoredFileRegexModel.TYPE)
public interface IgnoredFileRegexModel extends WindupVertexFrame {

    String TYPE = "IgnoredFileRegexModel";
    String NAME_REGEX = "nameRegex";
    String COMPILATION_ERROR = "compilationError";

    /**
     * Contains the regex pattern to filter out
     */
    @Property(NAME_REGEX)
    String getRegex();

    /**
     * Contains the regex pattern to filter out
     */
    @Property(NAME_REGEX)
    void setRegex(String regex);

    /**
     * Checks if the regex is compilable
     */
    @Property(COMPILATION_ERROR)
    String getCompilationError();

    @Property(COMPILATION_ERROR)
    void setCompilationError(String errorMessage);

}
