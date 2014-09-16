package org.jboss.windup.config.query;

import org.ocpsoft.rewrite.config.ConditionBuilder;

public interface QueryBuilderAs
{
    /**
     * Set the name of the output variable into which results of the {@link Query} will be stored.
     */
    ConditionBuilder as(String name);
}
