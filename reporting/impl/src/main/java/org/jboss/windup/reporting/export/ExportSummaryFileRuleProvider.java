package org.jboss.windup.reporting.export;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import freemarker.template.TemplateModelException;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DependentPhase;
import org.jboss.windup.config.phase.PostReportGenerationPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.exec.configuration.options.ExcludeTagsOption;
import org.jboss.windup.exec.configuration.options.IncludeTagsOption;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.traversal.ProjectModelTraversal;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.rules.AttachApplicationReportsToIndexRuleProvider;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.util.PathUtil;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RuleProvider generating optional analysis summary files for every application. This file will contain the main reporting information.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
@RuleMetadata(phase = DependentPhase.class,
        after = PostReportGenerationPhase.class,
        before = AttachApplicationReportsToIndexRuleProvider.class,
        haltOnException = true)
public class ExportSummaryFileRuleProvider  extends AbstractRuleProvider {

    // @formatter:off
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(Query.fromType(WindupConfigurationModel.class).withProperty(WindupConfigurationModel.SUMMARY_MODE, true))
                .perform(
                        Iteration.over(Iteration.DEFAULT_VARIABLE_LIST_STRING).perform(
                                new ExportSummaryFileRuleProvider.ExportSummaryReportOperation()).endIteration());
    }
    // @formatter:on

    private final class ExportSummaryReportOperation extends AbstractIterationOperation<WindupConfigurationModel> {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, WindupConfigurationModel config) {
            InlineHintService hintService = new InlineHintService(event.getGraphContext());
            String outputFolderPath = config.getOutputPath().getFilePath() + File.separator;
            Set<String> includeTags = event.getGraphContext().getOptionMap().get(IncludeTagsOption.NAME) == null ? new HashSet<>() :
                    new HashSet<>((Collection<String>)event.getGraphContext().getOptionMap().get(IncludeTagsOption.NAME));
            Set<String> excludeTags = event.getGraphContext().getOptionMap().get(ExcludeTagsOption.NAME) == null ? new HashSet<>() :
                    new HashSet<>((Collection<String>)event.getGraphContext().getOptionMap().get(ExcludeTagsOption.NAME));

            ClassificationService classificationService = new ClassificationService(event.getGraphContext());

            List<ProjectModel> projectModels = config.getInputPaths().stream().map(p -> p.getProjectModel()).collect(Collectors.toList());

            List <ProjectModelTraversal> traversals = projectModels.stream().map(m -> new ProjectModelTraversal(m)).collect(Collectors.toList());

            //Set<String> issueCategories = Set.of("mandatory", "optional", "potential", "cloud-mandatory", "cloud-optional", "information");
            Set<String> issueCategories = new HashSet<>();


            // Get values for classification and hints.
            traversals.forEach(t -> {
                Map<Integer, Integer> classificationIncidentsbyEffortLevel =
                        classificationService.getMigrationEffortByPoints(t, includeTags, excludeTags, issueCategories, true, false);
                Map<Integer, Integer> hintIncidentsbyEffortLevel =
                        hintService.getMigrationEffortByPoints(t, includeTags, excludeTags, issueCategories, true, false);

                Map<IssueCategoryModel, Integer> classificationEffortDetails =
                        classificationService.getMigrationEffortBySeverity(event, t, includeTags, excludeTags, issueCategories, true);
                Map<IssueCategoryModel, Integer> hintEffortDetails =
                        hintService.getMigrationEffortBySeverity(event, t, includeTags, excludeTags, issueCategories, true);

                Map<IssueCategoryModel, Integer> results = new HashMap<>(classificationEffortDetails.size() + hintEffortDetails.size());
                results.putAll(classificationEffortDetails);
                for (Map.Entry<IssueCategoryModel, Integer> entry : hintEffortDetails.entrySet()) {
                    if (!results.containsKey(entry.getKey()))
                        results.put(entry.getKey(), entry.getValue());
                    else
                        results.put(entry.getKey(), results.get(entry.getKey()) + entry.getValue());
                }

                Map<String, Integer> translatedResults = new HashMap<>();

                results.forEach((k, v) -> translatedResults.put(k.getName(), v));
                Map<String, Map<String,Integer>> resultsWithTitle = new HashMap<>();
                resultsWithTitle.put("Incidents By Category", translatedResults);

                ObjectMapper mapper = new ObjectMapper();
                String json;

                FileWriter writer = null;
                try {
                    json = mapper.writeValueAsString(resultsWithTitle);
                    String filename = PathUtil.cleanFileName(t.getCurrent().getRootFileModel().getFileName()) + ".json";
                    writer = new FileWriter(outputFolderPath + filename);
                    writer.write(json);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException("Couldn't convert given map to a JSON: " + e.getMessage());
                } catch (IOException ioe) {
                    throw new RuntimeException("Couldn't write summary to file: " + ioe.getMessage());
                } finally {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            });

        }

    }

}
