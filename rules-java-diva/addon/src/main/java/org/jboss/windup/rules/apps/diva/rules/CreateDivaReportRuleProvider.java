package org.jboss.windup.rules.apps.diva.rules;

import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import io.tackle.diva.windup.model.DivaAppModel;

/**
 * Creates a report of Diva transaction analysis.
 */
@RuleMetadata(phase = ReportGenerationPhase.class, id = "Create Diva Report")
public class CreateDivaReportRuleProvider extends AbstractRuleProvider {
    public static final String TEMPLATE_JPA_REPORT = "/reports/templates/jpa.ftl";
    public static final String REPORT_DESCRIPTION = "This report contains details Diva related resources that were found in the application.";

    private static final Logger LOG = Logging.get(CreateDivaReportRuleProvider.class);


    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        GraphOperation addReport = new GraphOperation() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context) {
                WindupConfigurationModel windupConfiguration = WindupConfigurationService
                        .getConfigurationModel(event.getGraphContext());

                for (FileModel inputPath : windupConfiguration.getInputPaths()) {
                    ProjectModel application = inputPath.getProjectModel();
                    if (application == null) {
                        throw new WindupException("Error, no project found in: " + inputPath.getFilePath());
                    }
                    createDivaReport(event.getGraphContext(), application);
                }
            }

            @Override
            public String toString() {
                return "CreateDivaReport";
            }
        };

        return ConfigurationBuilder.begin().addRule().perform(addReport);
    }

    private void createDivaReport(GraphContext context, ProjectModel application) 
    {
        GraphService<DivaAppModel> service = new GraphService<>(context, DivaAppModel.class);
        for (DivaAppModel app: service.findAll()) {
            LOG.info(app.toPrettyString());
        }
    }
}
