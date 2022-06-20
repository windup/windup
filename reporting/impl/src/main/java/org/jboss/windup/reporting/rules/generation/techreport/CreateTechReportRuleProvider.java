package org.jboss.windup.reporting.rules.generation.techreport;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TagModel;
import org.jboss.windup.reporting.model.TechReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.TagGraphService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the ReportModel for Tech stats report, and the data structure the template needs.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class)
public class CreateTechReportRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logger.getLogger(CreateTechReportRuleProvider.class.getName());

    private static final String TEMPLATE_PATH_PUNCH = "/reports/templates/techReport-punchCard.ftl";
    private static final String REPORT_NAME_PUNCH = "Technologies";
    private static final String REPORT_DESCRIPTION_PUNCH = "This report is a statistic of technologies occurrences in the input applications."
            + " It shows how the technologies are distributed and is mostly useful when analysing many applications.";

    private static final String TEMPLATE_PATH_BOXES = "/reports/templates/techReport-boxes.ftl";
    private static final String REPORT_NAME_BOXES = "Technologies";
    private static final String REPORT_DESCRIPTION_BOXES = "This report is a statistic of technologies occurrences in the input applications."
            + " It is an overview of what technologies are found in given project or a set of projects.";

    @Inject
    private TagServiceHolder tagServiceHolder;

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                /// TODO: Move this to a FeedTagStructureToGraphRuleProvider.
                .addRule()
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        new TagGraphService(event.getGraphContext()).feedTheWholeTagStructureToGraph(tagServiceHolder.getTagService());
                    }
                }).withId("feedTagsToGraph")
                .addRule()
                .perform(new CreateTechReportPunchCardOperation()).withId("createTechReport");
    }

    private class CreateTechReportPunchCardOperation extends GraphOperation {
        @Override
        public void perform(GraphRewrite event, EvaluationContext evCtx) {
            // Get sectors tag and rows tag references.
            TagGraphService tagGraphService = new TagGraphService(event.getGraphContext());
            TagModel sectorsTag = tagGraphService.getTagByName(TechReportModel.EDGE_TAG_SECTORS);
            TagModel rowsTag = tagGraphService.getTagByName(TechReportModel.EDGE_TAG_ROWS);
            if (null == sectorsTag) {
                // throw new WindupException("Tech report sectors tag, '" + TechReportModel.EDGE_TAG_SECTORS + "', not found.");
                LOG.severe("Tech report sectors tag, '" + TechReportModel.EDGE_TAG_SECTORS
                        + "', not found. The technology report will not be rendered.");
                return;
            }
            if (null == rowsTag) {
                // throw new WindupException("Tech report rows tag, '" + TechReportModel.EDGE_TAG_ROWS + "', not found.");
                LOG.severe("Tech report rows tag, '" + TechReportModel.EDGE_TAG_ROWS + "', not found. The technology report will not be rendered.");
                return;
            }

            Map<String, TechReportModel> appProjectToReportMap = new HashMap<>();

            // Create the boxes report models for each app.
            for (ProjectModel appModel : new ProjectService(event.getGraphContext()).getRootProjectModels()) {
                final TechReportModel appTechReport = createTechReportBoxes(event.getGraphContext(), appModel);
                appTechReport.setSectorsHolderTag(sectorsTag);
                appTechReport.setRowsHolderTag(rowsTag);
                appProjectToReportMap.put(appModel.getElement().id().toString(), appTechReport);
            }

            // Create the global report models.
            TechReportModel reportPunch = createTechReportPunchCard(event.getGraphContext());
            reportPunch.setSectorsHolderTag(sectorsTag);
            reportPunch.setRowsHolderTag(rowsTag);
            reportPunch.setAppProjectIdToReportMap(appProjectToReportMap);
        }

        private TechReportModel createTechReportPunchCard(
                GraphContext graphContext) {
            TechReportModel report = createTechReportBase(graphContext);
            report.setReportName(REPORT_NAME_PUNCH);
            report.setTemplatePath(TEMPLATE_PATH_PUNCH);
            report.setDescription(REPORT_DESCRIPTION_PUNCH);
            report.setReportIconClass("fa fa-rocket");
            report.setDisplayInGlobalApplicationIndex(true);
            report.setDisplayInApplicationReportIndex(true);

            new ReportService(graphContext).setUniqueFilename(report, "techReport-punch", "html");
            return report;
        }

        private TechReportModel createTechReportBoxes(GraphContext graphContext, ProjectModel appModel) {
            TechReportModel report = createTechReportBase(graphContext);
            report.setProjectModel(appModel);
            report.setDisplayInGlobalApplicationIndex(false);
            report.setDisplayInApplicationReportIndex(true);
            report.setReportName(REPORT_NAME_BOXES);
            report.setTemplatePath(TEMPLATE_PATH_BOXES);
            report.setDescription(REPORT_DESCRIPTION_BOXES);
            report.setReportIconClass("fa fa-rocket");

            // Set the filename for the report
            new ReportService(graphContext).setUniqueFilename(report, "techReport-" + appModel.getName(), "html");

            return new GraphService<>(graphContext, TechReportModel.class).addTypeToModel(report);
        }

        private TechReportModel createTechReportBase(GraphContext graphContext) {
            ApplicationReportService applicationReportService = new ApplicationReportService(graphContext);
            ApplicationReportModel report = applicationReportService.create();
            report.setTemplateType(TemplateType.FREEMARKER);
            report.setMainApplicationReport(false);
            report.setReportPriority(103);

            return new GraphService<>(graphContext, TechReportModel.class).addTypeToModel(report);
        }
    }
}
