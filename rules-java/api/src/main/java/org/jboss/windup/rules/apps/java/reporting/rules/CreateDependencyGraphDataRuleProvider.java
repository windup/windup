package org.jboss.windup.rules.apps.java.reporting.rules;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.jboss.windup.reporting.model.DependencyGraphDTO;
import org.jboss.windup.reporting.service.ApplicationDependencyService;
import org.jboss.windup.reporting.service.ReportService;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

/**
 * Generates a .js (javascript) file in the reports directory containing the
 * apps and their dependencies.
 */
@RuleMetadata(phase = ReportRenderingPhase.class)
public class CreateDependencyGraphDataRuleProvider extends AbstractRuleProvider {

	private static final String APP_DEPENDENCY_GRAPH_JS = "app_dependencies_graph.js";

	private static final String JS_DATA_FUNCTION_NAME = "app_dependencies";
	private static final String NEWLINE = System.lineSeparator();

	private static final ObjectMapper ITEMS_OBJECTMAPPER = getObjectMapperForItems(
			ApplicationDependencyGraphDTOItemSerializer.class);
	private static final ObjectMapper RELATIONS_OBJECTMAPPER = getObjectMapperForItems(
			ApplicationDependencyGraphDTORelationSerializer.class);

	@Override
	public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
		return ConfigurationBuilder.begin().addRule().perform(new GraphOperation() {
			@Override
			public void perform(GraphRewrite event, EvaluationContext context) {
				generateData(event);
			}
		});
	}

	// TODO: this could be faster
	private void generateData(GraphRewrite event) {
		//TODO: immediately return if sourceMode
		
		final GraphContext graphContext = event.getGraphContext();
		final ApplicationDependencyService appDependencyService = new ApplicationDependencyService(graphContext);
		final ReportService reportService = new ReportService(graphContext);

		final Map<String, DependencyGraphDTO> appDeps = appDependencyService.getAllDependenciesGraphData();

		try {
			List<FileModel> inputPaths = WindupConfigurationService.getConfigurationModel(graphContext).getInputPaths();
			
			// all apps
			if (inputPaths.size() > 1) {
				writeJsonData(APP_DEPENDENCY_GRAPH_JS, reportService, appDeps);
			}

			// per single app
			inputPaths.forEach( inputPath -> {
				final ProjectModel rootProjectModel = inputPath.getProjectModel();
				Map<String, DependencyGraphDTO> singleAppDependencies = null;
				if (inputPaths.size() > 1) {
					singleAppDependencies = appDependencyService.getDependenciesGraphDataByInputApp(rootProjectModel);
				} else {
					singleAppDependencies = appDeps;
				}
				try {
					writeJsonData(rootProjectModel.getRootFileModel().getSHA1Hash() + "_" + APP_DEPENDENCY_GRAPH_JS, reportService, singleAppDependencies);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
		} catch (Exception e) {
			throw new WindupException("Error serializing app dependency graph json due to: " + e.getMessage(), e);
		}
	}

	private void writeJsonData(final String jsFileName,final ReportService reportService, final Map<String, DependencyGraphDTO> appDeps) throws IOException {
		Path dataDirectory = reportService.getReportDataDirectory();

		final Path appDependencyGraphPath = dataDirectory.resolve(jsFileName);
		final List<String> synchronizedJson = Collections.synchronizedList(new ArrayList<>());
		
		synchronizedJson.add(JS_DATA_FUNCTION_NAME + "({" + NEWLINE + "\"items\": {");
		
		// items section
		appDeps.values().parallelStream().filter(value -> value != null && value.getSha1() != null).forEach(value -> {
			try {
				synchronizedJson.add(ITEMS_OBJECTMAPPER.writeValueAsString(value) + ",");
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		
		// relations section
		synchronizedJson.add("}, \"relations\":[");
		appDeps.values().parallelStream().forEach(value -> {
			try {
				synchronizedJson.add(RELATIONS_OBJECTMAPPER.writeValueAsString(value));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});
		synchronizedJson.add("]});");
		Files.write(appDependencyGraphPath, synchronizedJson);
	}

	private static ObjectMapper getObjectMapperForItems(Class<? extends StdSerializer<DependencyGraphDTO>> clazz) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			SimpleModule module = new SimpleModule();
			module.addSerializer(DependencyGraphDTO.class, clazz.getDeclaredConstructor().newInstance());
			mapper.registerModule(module);
			return mapper;
		} catch (Exception e) {
			// instead of failing we just return a regular mapper.
			// this might corrupt the app graph report,
			//but the analysis shouldn't fail in  that case
			return new ObjectMapper();
		}
	}	
}