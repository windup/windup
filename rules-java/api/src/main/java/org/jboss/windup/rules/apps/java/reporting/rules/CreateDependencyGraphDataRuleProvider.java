package org.jboss.windup.rules.apps.java.reporting.rules;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.GraphOperation;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.exec.configuration.options.ExplodedAppInputOption;
import org.jboss.windup.rules.apps.java.condition.SourceMode;
import org.jboss.windup.rules.apps.java.dependencyreport.DependenciesReportModel;
import org.jboss.windup.rules.apps.java.dependencyreport.DependencyReportDependencyGroupModel;
import org.jboss.windup.rules.apps.java.reporting.freemarker.dto.DependencyGraphItem;
import org.jboss.windup.rules.apps.java.reporting.freemarker.dto.DependencyGraphItemsAndRelations;
import org.jboss.windup.rules.apps.java.reporting.freemarker.dto.DependencyGraphRelation;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a .js (javascript) file in the reports directory containing the apps and their dependencies.
 */
@RuleMetadata(phase = ReportRenderingPhase.class)
public class CreateDependencyGraphDataRuleProvider extends AbstractRuleProvider {

    private static final String APP_DEPENDENCY_GRAPH_JS_FILENAME = "app_dependencies_graph.js";
    private static final String JS_DATA_FUNCTION_NAME = "app_dependencies";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(SourceMode.isDisabled())
                .perform(new GraphOperation() {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context) {
                        generateData(event);
                    }
                });
    }

    private void generateData(GraphRewrite event) {
        final GraphContext graphContext = event.getGraphContext();
        final ReportService reportService = new ReportService(graphContext);
        final List<DependenciesReportModel> dependenciesReportModels = graphContext.service(DependenciesReportModel.class)
                .findAll();

        dependenciesReportModels.stream().forEach(dependenciesReportModel -> {
            ProjectModel application = dependenciesReportModel.getProjectModel();

            // in case of shared libraries ("virtual" project) we do not
            // generate the dependency graph
            if (application != null && ProjectModel.TYPE_VIRTUAL.equals(application.getProjectType()))
                return;

            Path dataDirectory = reportService.getReportDataDirectory();
            List<DependencyReportDependencyGroupModel> dependencyReportDependencyGroupModels = dependenciesReportModel
                    .getArchiveGroups();
            Map<String, DependencyGraphItem> items = new HashMap<>(dependencyReportDependencyGroupModels.size() + 1);
            List<DependencyGraphRelation> relations = new ArrayList<>(dependencyReportDependencyGroupModels.size());

            Path appDependencyGraphPath;
            // application will be null in case of the global dependencies report
            // since it refers to multiple applications and not just one
            if (application != null) {
                DependencyGraphItem analyzedApplicationDependencyGraphItem = new DependencyGraphItem(
                        dependenciesReportModel.getProjectModel());
                String applicationHash = getSha1Hash(application.getRootFileModel());
                items.put(applicationHash, analyzedApplicationDependencyGraphItem);
                appDependencyGraphPath = dataDirectory.resolve(applicationHash + "_" + APP_DEPENDENCY_GRAPH_JS_FILENAME);
            } else {
                WindupConfigurationService.getConfigurationModel(event.getGraphContext()).getInputPaths().stream()
                        .filter(fileModel -> !ProjectModel.TYPE_VIRTUAL
                                .equals(fileModel.getProjectModel().getProjectType()))
                        .forEach(fileModel -> {
                            DependencyGraphItem analyzedApplicationDependencyGraphItem = new DependencyGraphItem(
                                    fileModel.getProjectModel());
                            String applicationHash = getSha1Hash(fileModel);
                            items.put(applicationHash, analyzedApplicationDependencyGraphItem);
                        });
                appDependencyGraphPath = dataDirectory.resolve(APP_DEPENDENCY_GRAPH_JS_FILENAME);
            }

            dependencyReportDependencyGroupModels.stream().forEach(dependencyReportDependencyGroupModel -> {
                DependencyGraphItem dependencyGraphItem = new DependencyGraphItem(dependencyReportDependencyGroupModel);
                items.put(dependencyReportDependencyGroupModel.getSHA1(), dependencyGraphItem);
                dependencyReportDependencyGroupModel.getArchives().stream().forEach(dependencyReportToArchiveEdgeModel -> {
                    FileModel targetFileModel;
                    // sometimes (especially in test cases) it could happen that there's no
                    // parent archive and just the root one
                    if (dependencyReportToArchiveEdgeModel.getArchive().getParentArchive() != null) {
                        targetFileModel = dependencyReportToArchiveEdgeModel.getArchive().getParentArchive();
                    } else {
                        targetFileModel = dependencyReportToArchiveEdgeModel.getArchive().getRootArchiveModel();
                        if (dependencyReportToArchiveEdgeModel.getArchive().equals(targetFileModel) &&
                                (Boolean) event.getGraphContext().getOptionMap().getOrDefault(ExplodedAppInputOption.NAME, Boolean.FALSE)
                                && application != null) {
                            targetFileModel = application.getRootFileModel();
                        }
                    }

                    DependencyGraphRelation dependencyGraphRelation = new DependencyGraphRelation(
                            dependencyReportDependencyGroupModel.getSHA1(),
                            getSha1Hash(targetFileModel));
                    relations.add(dependencyGraphRelation);
                });
            });

            DependencyGraphItemsAndRelations dependencyGraphItemsAndRelations = new DependencyGraphItemsAndRelations(items,
                    relations);
            try (FileWriter dependencyGraphAppDependenciesWriter = new FileWriter(appDependencyGraphPath.toFile())) {
                MappingJsonFactory jsonFactory = new MappingJsonFactory();
                jsonFactory.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
                ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
                objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

                dependencyGraphAppDependenciesWriter.write(JS_DATA_FUNCTION_NAME + "(");
                objectMapper.writer().writeValue(dependencyGraphAppDependenciesWriter, dependencyGraphItemsAndRelations);
                dependencyGraphAppDependenciesWriter.write(");");
                dependencyGraphAppDependenciesWriter.flush();
            } catch (IOException ioe) {
                throw new WindupException("Error serializing dependencies graph due to: " + ioe.getMessage(), ioe);
            }
        });
    }

    private String getSha1Hash(FileModel fileModel) {
        return !fileModel.isDirectory() ? fileModel.getSHA1Hash() : DigestUtils.sha1Hex(fileModel.getFileName());
    }
}
