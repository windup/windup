package org.jboss.windup.rules.apps.java.reporting.rules;


import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.Tokens;
import com.tinkerpop.gremlin.groovy.GremlinGroovyPipeline;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import com.tinkerpop.pipes.branch.LoopPipe;
import com.tinkerpop.pipes.filter.PropertyFilterPipe;
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
public class CreateUnparsableFilesReportRuleProvider2 extends AbstractRuleProvider
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
                ProjectModel rootProjectModel = payload.getInputPath().getProjectModel();
                if (rootProjectModel == null)
                    throw new WindupException("Error, no project found in: " + payload.getInputPath().getFilePath());

                createReportModel(event.getGraphContext(), rootProjectModel);
            }

            public String toString() { return "addReport"; }
        };

        // For each FileModel...
        AbstractIterationOperation<FileModel> addFilesToReport =
                new AbstractIterationOperation<FileModel>("file")
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, FileModel fileModel)
            {
                final Variables vars = Variables.instance(event);
                addFile(event.getGraphContext(), fileModel);
            }

            public String toString() { return "renderProjects"; }
        };

        Operation processProject = Iteration.over("project")
                .when(Query.from("project")
                        .piped(new InCriterion(FileModel.FILE_TO_PROJECT_MODEL))
                        .withProperty(PROPERTY_UNPARSABLE).as("files")
                )
                .perform(
                        Iteration.over("files").perform(addFilesToReport).endIteration()
                ).endIteration();

        return ConfigurationBuilder.begin()
        .addRule()
        .when(
            Query.fromType(WindupConfigurationModel.class).as("wc"),
            Query.fromType(ProjectModel.class).as("projects")
            //Query.from("projects").piped(new InCriterion(FileModel.FILE_TO_PROJECT_MODEL)).withProperty(PROPERTY_UNPARSABLE).as("files")
        )
        .perform(
            Iteration.over("wc").perform(createReportModel).endIteration(),
            Iteration.over("projects").as("project").perform(
                    new AbstractIterationOperation<ProjectModel>()
                    {
                        public void perform(GraphRewrite event, EvaluationContext context, ProjectModel payload)
                        {
                            addProjectAndFiles(event, payload);
                        }
                    }
            ).endIteration()
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
        reportM.setDisplayInApplicationList(false);
        reportM.setProjectModel(rootProjectModel);
        reportM.setTemplatePath(TEMPLATE_UNPARSABLE);
        reportM.setTemplateType(TemplateType.FREEMARKER);
        //applicationReportModel.setIncludeTags(includeTags);
        //applicationReportModel.setExcludeTags(excludeTags);

        //GraphService<OverviewReportLineMessageModel> lineNotesService = new GraphService<>(context, OverviewReportLineMessageModel.class);
        //Iterable<OverviewReportLineMessageModel> allLines = lineNotesService.findAll();

        // Set the filename for the report
        ReportService reportService = new ReportService(context);
        reportService.setUniqueFilename(reportM, REPORT_NAME + "_" + rootProjectModel.getName(), "html");

        ///UnparsablesAppReportModel check = reportService.getReportByName(REPORT_NAME, UnparsablesAppReportModel.class);
        ///System.out.println("CHECK: " + check);


        // Projects 1
        try {
            for (ProjectModel project : reportM.getAllProjects())
                System.out.println("PROJECT: " + project.toPrettyString());
        } catch (Exception ex){ System.out.println("EX: " + ex.getMessage()); }
        try {
            for (ProjectModel project : reportM.getAllProjects2())
                System.out.println("PROJECT 2: " + project.toPrettyString());
        } catch (Exception ex){ System.out.println("EX: " + ex.getMessage()); }
        try {
            for (ProjectModel project : reportM.getAllProjects3())
                System.out.println("PROJECT 3: " + project.toPrettyString());
        } catch (Exception ex){ System.out.println("EX: " + ex.getMessage()); }
        try {
            for (ProjectModel project : reportM.getChildProjects())
                System.out.println("CHILD PROJECTS: " + project.toPrettyString());
        } catch (Exception ex){ System.out.println("EX: " + ex.getMessage()); }
        try {
            for (ProjectModel project : reportM.getReportToProject())
                System.out.println("REP_TO_PROJECT: " + project.toPrettyString());
        } catch (Exception ex){ System.out.println("EX: " + ex.getMessage()); }


        // Projects 2
        final PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean> pipeFunction = new PipeFunction<LoopPipe.LoopBundle<Vertex>, Boolean>()
        {
            public Boolean compute(LoopPipe.LoopBundle<Vertex> argument)
            {
                return Boolean.TRUE;
            }
        };

        GremlinPipeline<Vertex, Vertex> projectsPipe =
                new GremlinGroovyPipeline<Vertex, Vertex>(rootProjectModel.asVertex())
                .in(ProjectModel.PARENT_PROJECT).loop(1, pipeFunction);
        for (Vertex projectV : projectsPipe)
        {
            ProjectModel project = context.getFramed().frame(projectV, ProjectModel.class);
            System.out.println("PROJECT: " + project.toPrettyString());
        }

        SoutPipe soutPipe = new SoutPipe(context, FileModel.class);
        SoutPipe soutPipe2 = new SoutPipe(context, ProjectModel.class);

        // Trying multi-iteration
        Iterator<? extends Element> unparsables = new GremlinGroovyPipeline<Vertex, Vertex>(reportM.getProjectModel())
                //.add(new WindupTypePipe(FileModel.class))
                .sideEffect(soutPipe)
                .in(FileModel.FILE_TO_PROJECT_MODEL)
                .has(PROPERTY_UNPARSABLE)
                .sideEffect(soutPipe2)
                .iterator();
    }




    private void addFile(GraphContext context, FileModel fileModel)
    {
        //Variables.instance(context).
    }

    private void addProjectAndFiles(GraphRewrite event, ProjectModel payload)
    {
        //UnparsablesAppReportModel reportM = new ReportService(event.getGraphContext()).getReportByName(REPORT_NAME, UnparsablesAppReportModel.class);
        //ProjectModel rootProjectM = new ProjectService(event.getGraphContext()).getRootProject();
    }


    private static class WindupTypePipe extends PropertyFilterPipe<Vertex, Vertex>
    {
        public WindupTypePipe(Class<FileModel> clazz)
        {
            super(WindupVertexFrame.TYPE_PROP, Tokens.mapPredicate(Tokens.T.in), GraphTypeManager.getTypeIdentifier(clazz));
            //String typeId = GraphTypeManager.getTypeIdentifier(clazz);
            //final Pipe pipe = new PropertyFilterPipe(WindupVertexFrame.TYPE_PROP, Tokens.mapPredicate(Tokens.T.in), typeId);
        }
    }


    private static class SoutPipe implements PipeFunction
    {
        private final GraphContext grCtx;
        private final Class<? extends WindupVertexFrame> frameType;

        private SoutPipe(GraphContext context, Class<? extends WindupVertexFrame> frameType)
        {
            this.grCtx = context;
            this.frameType = frameType;
        }

        @Override
        public Vertex compute(Object argument)
        {
            System.out.println(grCtx.getFramed().frame((Vertex)argument, frameType).toPrettyString());
            return (Vertex)argument;
        }
    };

}
