package org.jboss.windup.reporting.rules.generation.techreport;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportGenerationPhase;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ApplicationProjectModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.*;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.reporting.service.TagGraphService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Creates the ReportModel for Tech stats report (boxes version), and the data structure the template needs.
 *
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(phase = ReportGenerationPhase.class, after = CreateTechReportPunchCardRuleProvider.class)
public class CreateTechReportBoxesRuleProvider extends AbstractRuleProvider
{
    public static final Logger LOG = Logger.getLogger(CreateTechReportBoxesRuleProvider.class.getName());


    public static final String TEMPLATE_PATH = "/reports/templates/techReport-boxes.ftl";
    private static final String REPORT_NAME = "Technologies boxes";
    public static final String REPORT_DESCRIPTION =
            "This report is a statistic of technologies occurences in the input applications."
            + " It is an overview of what techogies are found in given project or a set of projects.";

    @Inject private TagServiceHolder tagServiceHolder;

    @Override
    public void put(Object key, Object value)
    {
        super.put(key, value);
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new CreateTechReportBoxesOperation());
    }

    private class CreateTechReportBoxesOperation extends GraphOperation
    {


        @Override
        public void perform(GraphRewrite event, EvaluationContext evCtx)
        {
            GraphContext grCtx = event.getGraphContext();

            // Create the report model.
            TechReportPunchCardModel report = createBoxesReport(grCtx);

            // Add sectors to it.
            TagGraphService tagGraphService = new TagGraphService(event.getGraphContext());
            TagModel sectorsTag = tagGraphService.getTagByName(TechReportPunchCardModel.EDGE_TAG_SECTORS);
            TagModel rowsTag = tagGraphService.getTagByName(TechReportPunchCardModel.EDGE_TAG_ROWS);
            if (null == sectorsTag)
                throw new WindupException("Tech report sectors tag, '" + TechReportPunchCardModel.EDGE_TAG_SECTORS + "', not found.");
            if (null == rowsTag)
                throw new WindupException("Tech report rows tag, '" + TechReportPunchCardModel.EDGE_TAG_ROWS + "', not found.");
            report.setSectorsHolderTag(sectorsTag);
            report.setRowsHolderTag(rowsTag);
        }
    }

    private static TechReportPunchCardModel createBoxesReport(GraphContext grCtx)
    {
        TechReportPunchCardModel report = createTechReportBase(grCtx);
        ReportService reportService = new ReportService(grCtx);
        reportService.setUniqueFilename(report, "techReport-boxes", "html");
        report.setReportName(REPORT_NAME);
        report.setTemplatePath(TEMPLATE_PATH);
        report.setDescription(REPORT_DESCRIPTION);
        return report;
    }
    private static TechReportPunchCardModel createTechReportBase(GraphContext grCtx)
    {
        ApplicationReportService applicationReportService = new ApplicationReportService(grCtx);
        ApplicationReportModel report = applicationReportService.create();
        report.setTemplateType(TemplateType.FREEMARKER);
        report.setDisplayInApplicationReportIndex(true);
        report.setDisplayInGlobalApplicationIndex(true);
        report.setReportPriority(101);
        report.setReportIconClass("glyphicon glyphicon-tags");

        TechReportPunchCardModel techReport = new GraphService<>(grCtx, TechReportPunchCardModel.class).addTypeToModel(report);
        return techReport;
    }


}
