package org.jboss.windup.reporting.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.IgnoredFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.DependencyGraphDTO;

@Singleton
public class ApplicationDependencyService extends GraphService<ArchiveModel> {

	private static final String SHARED_LIBS_PROJECT_TYPE = "VIRTUAL";
	
	private List<IgnoredFileModel> ignoredFiles = new ArrayList<>();
	
	private Map<String, DependencyGraphDTO> appDeps = new HashMap<>();

	public ApplicationDependencyService(GraphContext context) {
		super(context, ArchiveModel.class);
	}

	public Map<String, DependencyGraphDTO> getAllDependenciesGraphData() {
		if (MapUtils.isEmpty(appDeps)) {
			WindupConfigurationService.getConfigurationModel(getGraphContext())
			.getInputPaths().parallelStream().forEach( inputPath -> {
				final ProjectModel rootProjectModel = inputPath.getProjectModel();
				GraphService<IgnoredFileModel> ignoredFilesModelService = new GraphService<>(getGraphContext(), IgnoredFileModel.class);
		        ignoredFiles = ignoredFilesModelService.findAll();
		        
				appDeps.computeIfAbsent(rootProjectModel.getRootFileModel().getSHA1Hash(), value -> new DependencyGraphDTO(rootProjectModel, false, false));
				rootProjectModel.getAllProjectModels().parallelStream().forEach(item -> addChildren(item));
			});
		}
		return MapUtils.unmodifiableMap(appDeps);
	}
	
	// TODO: if only this method is called  and not all data is needed, it might be suboptimal.
	// it's hacked together in an evening with a drink in my hand...
	// TODO: get rid off duplicate code
	public Map<String, DependencyGraphDTO> getDependenciesGraphDataByInputApp(final ProjectModel projectModel) {
		if (MapUtils.isEmpty(appDeps)) { // just to make sure we already have all the data collected
			getAllDependenciesGraphData();
		}
		// return the whole map in case there is just one inputPath
		if (WindupConfigurationService.getConfigurationModel(getGraphContext()).getInputPaths().size() == 1) {
			return Collections.unmodifiableMap(appDeps);
		}
		
		final String sha1OfRoot = projectModel.getRootFileModel().getSHA1Hash();
		
		// first get all entries in the map which are related to this entry
		 Map<String, DependencyGraphDTO> workingSet = new HashMap<>();
				 appDeps.entrySet().forEach( item -> {
			if(sha1OfRoot.equals(item.getKey())) {
				workingSet.put(item.getKey(), item.getValue());
		}});
		
		// now add all of the children
		 addChildrenSelected(projectModel, workingSet);
		 
		return Collections.unmodifiableMap(workingSet);
	}
	
	private void addChildrenSelected(final ProjectModel projectModel, Map<String, DependencyGraphDTO> appDepsSingleApp) {
		if (CollectionUtils.isEmpty(projectModel.getChildProjects())) {
			return;
		}
		//filter out shared libs - this is just a virtual construct and not relevant for the app dependency graph
		List<ProjectModel> children = projectModel.getChildProjects().parallelStream().filter(
				item -> !StringUtils.equalsIgnoreCase(SHARED_LIBS_PROJECT_TYPE, item.getProjectType())).collect(Collectors.toList());
		children.stream().forEach(item -> {
			final String sha1Hash = item.getRootFileModel().getSHA1Hash();
			if (appDeps.containsKey(sha1Hash)) {
				appDepsSingleApp.putIfAbsent(sha1Hash, appDeps.get(sha1Hash));
				addChildrenSelected(item, appDepsSingleApp);
			}
		});
	}
	
	private void addChildren(ProjectModel projectModel) {
		if (CollectionUtils.isEmpty(projectModel.getChildProjects())) {
			return;
		}
		
		//filter out shared libs - this is just a virtual construct and not relevant for the app dependency graph
		List<ProjectModel> children = projectModel.getChildProjects().parallelStream().filter(
				item -> !StringUtils.equalsIgnoreCase(SHARED_LIBS_PROJECT_TYPE, item.getProjectType())).collect(Collectors.toList());
		
		children.parallelStream().forEach(item -> {
			boolean isIgnored = isFileIgnored(item);
			final DependencyGraphDTO dto = appDeps.computeIfAbsent(item.getRootFileModel().getSHA1Hash(),
					value -> new DependencyGraphDTO(item, true, isIgnored));
			dto.addParent(projectModel.getRootFileModel().getSHA1Hash());
			addChildren(item);
		});
	}

	private boolean isFileIgnored(ProjectModel item) {
        return ignoredFiles.parallelStream().anyMatch(ignoredFile -> ignoredFile.getSHA1Hash().equals(item.getRootFileModel().getSHA1Hash()));
	}
}