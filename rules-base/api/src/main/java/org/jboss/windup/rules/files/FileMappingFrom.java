package org.jboss.windup.rules.files;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Intermediate step for configuring the {@link FileMapping} rule.
 * 
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FileMappingFrom
{
    /**
     * Specifies the {@link WindupVertexFrame} type to which files matching the specified pattern should be mapped.
     */
    Rule to(Class<? extends WindupVertexFrame> type);

    /**
     * Specifies the {@link WindupVertexFrame} types to which files matching the specified pattern should be mapped.
     */
    Rule to(@SuppressWarnings("unchecked") Class<? extends WindupVertexFrame>... types);
}
