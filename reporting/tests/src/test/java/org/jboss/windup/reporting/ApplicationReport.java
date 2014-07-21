package org.jboss.windup.reporting;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

public class ApplicationReport extends GraphOperation
{
    private String applicationName;
    private String applicationVersion;
    private String applicationCreator;

    public static ApplicationReport create()
    {
        return new ApplicationReport();
    }

    public ApplicationReport applicationName(String applicationName)
    {
        this.applicationName = applicationName;
        return this;
    }

    public ApplicationReport applicationVersion(String applicationVersion)
    {
        this.applicationVersion = applicationVersion;
        return this;
    }

    public ApplicationReport applicationCreator(String applicationCreator)
    {
        this.applicationCreator = applicationCreator;
        return this;
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context)
    {
        ApplicationReportModel appModel = event.getGraphContext().getFramed()
                    .addVertex(null, ApplicationReportModel.class);
        appModel.setApplicationName(this.applicationName);
        appModel.setApplicationVersion(this.applicationVersion);
        appModel.setApplicationCreator(this.applicationCreator);
    }
}
