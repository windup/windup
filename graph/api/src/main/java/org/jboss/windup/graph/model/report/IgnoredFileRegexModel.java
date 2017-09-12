package org.jboss.windup.graph.model.report;

import org.jboss.windup.graph.model.WindupVertexFrame;

import com.tinkerpop.frames.Property;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Indicates which files within archives should be skipped by Windup.
 */
@TypeValue(IgnoredFileRegexModel.TYPE)
public interface IgnoredFileRegexModel extends WindupVertexFrame
{

    public static final String TYPE = "IgnoredFileRegexModel";
    String NAME_REGEX = "nameRegex";
    String COMPILATION_ERROR = "compilationError";

    /**
     * Contains the regex pattern to filter out
     */
    @Property(NAME_REGEX)
    public String getRegex();

    /**
     * Contains the regex pattern to filter out
     */
    @Property(NAME_REGEX)
    public void setRegex(String regex);
    
    /**
     * Checks if the regex is compilable
     */
    @Property(COMPILATION_ERROR)
    public String getCompilationError();
    
    @Property(COMPILATION_ERROR)
    public void setCompilationError(String errorMessage);

}
