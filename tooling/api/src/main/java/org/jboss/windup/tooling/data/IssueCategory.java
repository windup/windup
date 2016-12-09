package org.jboss.windup.tooling.data;

import java.io.Serializable;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
public interface IssueCategory extends Serializable
{
    String getCategoryID();

    String getOrigin();

    String getName();

    String getDescription();

    Integer getPriority();
}
