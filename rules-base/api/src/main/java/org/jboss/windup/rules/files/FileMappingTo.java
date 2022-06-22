package org.jboss.windup.rules.files;

import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel.OnParseError;
import org.ocpsoft.rewrite.config.Rule;

/**
 * Intermediate step for configuring the {@link FileMapping} rule.
 *
 * @author <a href="mailto:lincolnbaxter@gmail.com">Lincoln Baxter, III</a>
 */
public interface FileMappingTo extends Rule {
    /**
     * Specifies the {@link WindupVertexFrame} type to which files matching the specified pattern should be mapped.
     */
    FileMappingTo onParseError(OnParseError onParseError);

    /**
     * Attach an ID to this rule.
     */
    FileMappingWithID withId(String ruleID);
}
