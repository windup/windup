package org.jboss.windup.rules.apps.java.reporting.rules;


import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.groovy.GremlinGroovyPipeline;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.filter.PropertyFilterPipe;
import com.tinkerpop.pipes.util.structures.Row;
import com.tinkerpop.pipes.util.structures.Table;
import java.util.Iterator;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.query.InCriterion;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Operation;
import org.ocpsoft.rewrite.context.EvaluationContext;


/**
 * Creates the main report HTML page for a Java application.
 */
@RuleMetadata(
        phase = ReportGenerationPhase.class
)
public class CreateUnparsableFilesReportRuleProvider extends AbstractRuleProvider
{
    public static final String REPORT_NAME = "Unparsable";
    public static final String TEMPLATE_UNPARSABLE = "/reports/templates/unparsable_files.ftl";
    public static final String PROPERTY_UNPARSABLE = "w:unparsable";

    // @formatter:off
    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        // Create the ReportModel.
        AbstractIterationOperation<WindupConfigurationModel> createReportModel =
                new AbstractIterationOperation<WindupConfigurationModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel payload)
            {
                for(FileModel fileM : payload.getInputPaths()){
                    ProjectModel rootProjectModel = fileM.getProjectModel();
                    if (rootProjectModel == null)
                        throw new WindupException("Error, no project found in: " + fileM.getFilePath());

                    createReportModel(event.getGraphContext(), rootProjectModel);
                }
            }

            public String toString() { return "addReport"; }
        };

        // For each FileModel...
        return ConfigurationBuilder.begin()
        .addRule()
        .when(
            Query.fromType(WindupConfigurationModel.class).as("wc"),
            Query.fromType(ProjectModel.class).as("projects")
        )
        .perform(
            Iteration.over("wc").perform(createReportModel).endIteration()
        );

    }
    // @formatter:on


    private void createReportModel(GraphContext context, ProjectModel rootProjectModel)
    {
        GraphService<UnparsablesAppReportModel> service = new GraphService<>(context, UnparsablesAppReportModel.class);
        UnparsablesAppReportModel reportM = service.create();
        reportM.setReportPriority(120);
        reportM.setDisplayInApplicationReportIndex(true);
        reportM.setReportName(REPORT_NAME);
        reportM.setReportIconClass("glyphicon glyphicon-home");
        reportM.setMainApplicationReport(false);
        reportM.setProjectModel(rootProjectModel);
        reportM.setTemplatePath(TEMPLATE_UNPARSABLE);
        reportM.setTemplateType(TemplateType.FREEMARKER);

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(reportM, REPORT_NAME + "_" + rootProjectModel.getName(), "html");
    }


    private static class WindupTypePipe extends PropertyFilterPipe<Vertex, Vertex>
    {
        public WindupTypePipe(Class<FileModel> clazz)
        {
            super(WindupVertexFrame.TYPE_PROP, Tokens.mapPredicate(Tokens.T.in), GraphTypeManager.getTypeIdentifier(clazz));
        }
    }

}
