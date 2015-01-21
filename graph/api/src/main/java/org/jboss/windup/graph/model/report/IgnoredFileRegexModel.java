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

    public static final String TYPE = "IgnoredFileRegex";

    /**
     * Contains the regex pattern to filter out
     */
    @Property("name_regex")
    public String getRegex();

    /**
     * Contains the regex pattern to filter out
     */
    @Property("name_regex")
    public void setRegex(String regex);
    
    /**
     * Checks if the regex is compilable
     */
    @Property("compilationError")
    public String getCompilationError();
    
    @Property("compilationError")
    public void setCompilationError(String errorMessage);

}
