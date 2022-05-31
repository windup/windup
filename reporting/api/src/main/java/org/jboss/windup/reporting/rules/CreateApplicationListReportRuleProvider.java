package org.jboss.windup.reporting.rules;

import org.jboss.forge.furnace.Furnace;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.LabelProvider;
import org.jboss.windup.config.loader.LabelLoader;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.metadata.LabelProviderRegistry;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.ProjectService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ApplicationReportModel;
import org.jboss.windup.reporting.model.TemplateType;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.ApplicationReportService;
import org.ocpsoft.logging.Logger;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
public class CreateApplicationListReportRuleProvider extends AbstractRuleProvider {
    public static final String APPLICATION_LIST_REPORT = "Application List";
    public static final String TEMPLATE_PATH = "/reports/templates/application_list.ftl";
    private static final Logger LOG = Logger.getLogger(CreateApplicationListReportRuleProvider.class);
    private static final String OUTPUT_FILENAME = "../index.html";
    @Inject
    private Furnace furnace;

    @Inject
    private LabelLoader labelLoader;

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
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

    private void createIndexReport(GraphContext context) {
        WindupConfigurationModel cfg = WindupConfigurationService.getConfigurationModel(context);
        List<Path> userLabelPaths = cfg.getUserLabelsPaths().stream().map(fileModel -> fileModel.asFile().toPath()).collect(Collectors.toList());

        // Load all labels from xml files
        List<Label> labels = new ArrayList<>();
        RuleLoaderContext labelLoaderContext = new RuleLoaderContext(userLabelPaths, null);
        LabelProviderRegistry labelProviderRegistry = labelLoader.loadConfiguration(labelLoaderContext);
        for (LabelProvider provider : labelProviderRegistry.getProviders()) {
            labels.addAll(provider.getData().getLabels());
        }

        JsonArrayBuilder labelsJsonArrayBuilder = Json.createArrayBuilder();
        for (Label label : labels) {
            labelsJsonArrayBuilder.add(toJson(label));
        }
        JsonArray labelsJsonArray = labelsJsonArrayBuilder.build();

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
        properties.put("target_runtimes", labelsJsonArray.toString());
        report.setReportProperties(properties);

        GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
        Map<String, WindupVertexFrame> relatedData = new HashMap<>();
        final Iterable<ApplicationReportModel> apps = applicationReportService.findAll();
        List<ApplicationReportModel> appsList = new ArrayList<>();
        for (ApplicationReportModel applicationReportModel : apps) {
            if (applicationReportModel.isMainApplicationReport() != null && applicationReportModel.isMainApplicationReport()) {
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

    private JsonObject toJson(Label label) {
        JsonArrayBuilder supportedJsonArrayBuilder = Json.createArrayBuilder();
        JsonArrayBuilder unsuitableJsonArrayBuilder = Json.createArrayBuilder();
        JsonArrayBuilder neutralJsonArrayBuilder = Json.createArrayBuilder();

        label.getSupported().forEach(supportedJsonArrayBuilder::add);
        label.getUnsuitable().forEach(unsuitableJsonArrayBuilder::add);
        label.getNeutral().forEach(neutralJsonArrayBuilder::add);

        return Json.createObjectBuilder()
                .add("id", label.getId())
                .add("name", label.getName())
                .add("description", label.getDescription() != null ? label.getDescription() : "")
                .add("supported", supportedJsonArrayBuilder.build())
                .add("unsuitable", unsuitableJsonArrayBuilder.build())
                .add("neutral", neutralJsonArrayBuilder.build())
                .build();
    }

    public static class AppRootFileNameComparator implements Comparator<ApplicationReportModel> {
        public AppRootFileNameComparator() {
        }

        public int compare(ApplicationReportModel o1, ApplicationReportModel o2) {
            // If the info is missing, put that to the end. This may be the case of virtual apps.
            if (null == o1.getProjectModel() || null == o1.getProjectModel().getRootFileModel() || null == o1.getProjectModel().getRootFileModel().getFileName())
                return 1;
            if (null == o2.getProjectModel() || null == o2.getProjectModel().getRootFileModel() || null == o2.getProjectModel().getRootFileModel().getFileName())
                return -1;

            try {
                return o1.getProjectModel().getRootFileModel().getFileName().compareToIgnoreCase(o2.getProjectModel().getRootFileModel().getFileName());
                //return Comparator.comparing((ApplicationReportModel o) -> o.getProjectModel().getRootFileModel().getFileName(), String::compareToIgnoreCase).compare(o1, o2);
            } catch (Throwable ex) {
                return 0;
            }
        }
    }
}
