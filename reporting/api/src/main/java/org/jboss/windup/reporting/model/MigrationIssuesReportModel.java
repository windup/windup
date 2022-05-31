package org.jboss.windup.reporting.model;

import org.jboss.windup.graph.model.TypeValue;

/**
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(MigrationIssuesReportModel.TYPE)
public interface MigrationIssuesReportModel extends ApplicationReportModel, IncludeAndExcludeTagsModel {
    String TYPE = "MigrationIssuesReportModel";
}
