package org.jboss.windup.reporting.rules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.json.*;

import org.jboss.forge.furnace.Furnace;
import org.jboss.forge.furnace.util.Visitor;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.jboss.windup.util.file.FileSuffixPredicate;
import org.jboss.windup.util.file.FileVisit;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This renders an application index page listing all applications analyzed by the current execution of windup.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:zizka@seznam.cz">Ondrej Zizka</a>
 */
@RuleMetadata(
        phase = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true
)
public class CreateApplicationListReportRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(CreateApplicationListReportRuleProvider.class);

    private static final String XML_EXTENSION = "\\.windup\\.json";
    public static final String APPLICATION_LIST_REPORT = "Application List";
    private static final String OUTPUT_FILENAME = "../index.html";
    public static final String TEMPLATE_PATH = "/reports/templates/application_list.ftl";

    @Inject
    private Furnace furnace;


    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            .addRule()
            .perform(new GraphOperation() {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context) {
                    createIndexReport(event.getGraphContext());
                }
            });
    }
    // @formatter:on

    private void createIndexReport(GraphContext context)
    {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);
        for (FileModel userRulesFileModel : cfg.getUserlabelsPaths())
        {
            FileSuffixPredicate fileSuffixPredicate = new FileSuffixPredicate(XML_EXTENSION);
            if (userRulesFileModel.isDirectory()) {
                Visitor<File> visitor = new Visitor<File>()
                {
                    @Override
                    public void visit(File file)
                    {
                        concatFileContentToArray(arrayBuilder, file);
                    }
                };
                FileVisit.visit(userRulesFileModel.asFile(), fileSuffixPredicate, visitor);
            } else {
                File file = userRulesFileModel.asFile();
                if (fileSuffixPredicate.accept(file)) {
                    concatFileContentToArray(arrayBuilder, file);
                }
            }
        }
        JsonArray jsonArray = arrayBuilder.build();


        ApplicationReportService applicationReportService = new ApplicationReportService(context);

        ApplicationReportModel report = applicationReportService.create();
        report.setReportPriority(1);
        report.setReportIconClass("glyphicon glyphicon-home");
        report.setReportName(APPLICATION_LIST_REPORT);
        report.setTemplatePath(TEMPLATE_PATH);
        report.setTemplateType(TemplateType.FREEMARKER);

        report.setDisplayInApplicationReportIndex(false);
        report.setReportFilename(OUTPUT_FILENAME);

        Map<String, String> properties = new HashMap<>();
        properties.put("target_runtimes", jsonArray.toString());
        report.setReportProperties(properties);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> relatedData = new HashMap<>();
        final Iterable<ApplicationReportModel> apps = applicationReportService.findAll();
        List<ApplicationReportModel> appsList = new ArrayList();
        for (ApplicationReportModel applicationReportModel : apps)
        {
            if (applicationReportModel.isMainApplicationReport() != null && applicationReportModel.isMainApplicationReport())
            {
                appsList.add(applicationReportModel);
                if (ProjectService.SHARED_LIBS_UNIQUE_ID.equals(applicationReportModel.getProjectModel().getUniqueID()))
                    relatedData.put("sharedLibsApplicationReport", applicationReportModel); // Used as kind of boolean in the template.
            }
        }

        // Our current model doesn't keep the list order, but once I wrote, I'm leaving the sorting here for when it does.
        Collections.sort(appsList, new AppRootFileNameComparator());
        WindupVertexListModel<ApplicationReportModel> appsListVertex = listService.create();
        relatedData.put("applications", appsListVertex);
        for (ApplicationReportModel applicationReportModel : appsList)
            appsListVertex.addItem(applicationReportModel);

        report.setRelatedResource(relatedData);
    }

    private void concatFileContentToArray(JsonArrayBuilder arrayBuilder, File file) {
        JsonReader reader = null;
        try {
            FileInputStream is = new FileInputStream(file);
            reader = Json.createReader(is);
            JsonArray array = reader.readArray();

            for (JsonValue obj: array) {
                arrayBuilder.add(obj);
            }
        } catch (FileNotFoundException e) {
            // Nothing to do
            LOG.error(e.getCause().getMessage());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }


    public static class AppRootFileNameComparator implements Comparator<ApplicationReportModel>
    {
        public AppRootFileNameComparator()
        {
        }

        public int compare(ApplicationReportModel o1, ApplicationReportModel o2)
        {
            // If the info is missing, put that to the end. This may be the case of virtual apps.
            if (null == o1.getProjectModel() || null == o1.getProjectModel().getRootFileModel() || null == o1.getProjectModel().getRootFileModel().getFileName() )
                return 1;
            if (null == o2.getProjectModel() || null == o2.getProjectModel().getRootFileModel() || null == o2.getProjectModel().getRootFileModel().getFileName() )
                return -1;

            try {
                return o1.getProjectModel().getRootFileModel().getFileName().compareToIgnoreCase(o2.getProjectModel().getRootFileModel().getFileName());
                //return Comparator.comparing((ApplicationReportModel o) -> o.getProjectModel().getRootFileModel().getFileName(), String::compareToIgnoreCase).compare(o1, o2);
            }
            catch (Throwable ex)
            {
                return 0;
            }
        }
    }
}
