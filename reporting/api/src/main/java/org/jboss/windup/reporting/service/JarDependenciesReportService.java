package org.jboss.windup.reporting.service;

import java.util.logging.Logger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.JarDependenciesReportModel;
import org.jboss.windup.util.Logging;

/**
 * This class provides helpful utility methods for creating and finding {@link ApplicationReportModel} vertices.
 * 
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * 
 */
public class JarDependenciesReportService extends GraphService<JarDependenciesReportModel>
{
    private static final Logger LOG = Logging.get(JarDependenciesReportService.class);

    public JarDependenciesReportService(GraphContext context)
    {
        super(context, JarDependenciesReportModel.class);
    }

    /**
     * Overrides GraphService.create() to create the object with some reasonable defaults
     */
    public JarDependenciesReportModel create()
    {
        JarDependenciesReportModel applicationReportModel = super.create();
        applicationReportModel.setDisplayInApplicationReportIndex(true);
        applicationReportModel.setMainApplicationReport(false);
        applicationReportModel.setReportPriority(120);
        return applicationReportModel;
    }

}
