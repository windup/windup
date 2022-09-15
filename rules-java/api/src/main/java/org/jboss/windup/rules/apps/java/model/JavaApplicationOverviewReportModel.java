package org.jboss.windup.rules.apps.java.model;

import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.IncludeAndExcludeTagsModel;

import org.jboss.windup.graph.model.TypeValue;

/**
 * Represents an overview of the Java application.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@TypeValue(JavaApplicationOverviewReportModel.TYPE)
public interface JavaApplicationOverviewReportModel extends ApplicationReportModel, IncludeAndExcludeTagsModel {
    String TYPE = "JavaApplicationOverviewReportModel";


}
