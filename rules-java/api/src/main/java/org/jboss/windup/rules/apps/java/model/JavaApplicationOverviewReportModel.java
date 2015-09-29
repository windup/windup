package org.jboss.windup.rules.apps.java.model;

import java.util.Set;

import org.jboss.windup.graph.SetInProperties;
import org.jboss.windup.reporting.model.ApplicationReportModel;

import com.tinkerpop.frames.modules.typedgraph.TypeValue;

/**
 * Represents an overview of the Java application.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaApplicationOverviewReportModel.TYPE)
public interface JavaApplicationOverviewReportModel extends ApplicationReportModel
{
    String TYPE = "JavaApplicationOverviewReport";
    String INCLUDE_TAGS = "includeTags";
    String EXCLUDE_TAGS = "excludeTags";

    /**
     * Set the set of tags to include in this report.
     */
    @SetInProperties(propertyPrefix = INCLUDE_TAGS)
    JavaApplicationOverviewReportModel setIncludeTags(Set<String> tags);

    /**
     * Get the set of tags to include in this report.
     */
    @SetInProperties(propertyPrefix = INCLUDE_TAGS)
    Set<String> getIncludeTags();

    /**
     * Set the set of tags to exclude from this report.
     */
    @SetInProperties(propertyPrefix = EXCLUDE_TAGS)
    JavaApplicationOverviewReportModel setExcludeTags(Set<String> tags);

    /**
     * Get the set of tags to exclude from this report.
     */
    @SetInProperties(propertyPrefix = EXCLUDE_TAGS)
    Set<String> getExcludeTags();
}
