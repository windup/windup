package org.jboss.windup.rules.files;

import org.jboss.windup.graph.model.WindupVertexFrame;

/**
 * Intermediate step for configuring the {@link FileMapping} rule.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FileMappingFrom {
    /**
     * Specifies the {@link WindupVertexFrame} type to which files matching the specified pattern should be mapped.
     */
    FileMappingTo to(Class<? extends WindupVertexFrame> type);

    /**
     * Specifies the {@link WindupVertexFrame} types to which files matching the specified pattern should be mapped.
     */
    FileMappingTo to(@SuppressWarnings("unchecked") Class<? extends WindupVertexFrame>... types);
}
