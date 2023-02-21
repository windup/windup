package org.jboss.windup.reporting.data.rules;

//@RuleMetadata(
//        phase = ReportPf4RenderingPhase.class,
//        haltOnException = true
//)
public class ApplicationHardcodedIpAddressesRuleProvider /*extends AbstractApiRuleProvider*/ {

//    public static final String PATH = "hardcoded-ip-addresses";
//
//    @Override
//    public String getBasePath() {
//        return PATH;
//    }
//
//    @Override
//    public Object getAll(GraphRewrite event) {
//        GraphContext context = event.getGraphContext();
//        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
//        GraphService<HardcodedIPLocationModel> ipLocationModelService = new GraphService<>(context, HardcodedIPLocationModel.class);
//
//        List<ApplicationHardcodedIpAddressesDto> result = new ArrayList<>();
//
//        for (FileModel inputPath : configurationModel.getInputPaths()) {
//            ProjectModel application = inputPath.getProjectModel();
//
//            ApplicationHardcodedIpAddressesDto applicationHardcodedIpAddressesDto = new ApplicationHardcodedIpAddressesDto();
//            applicationHardcodedIpAddressesDto.applicationId = application.getId().toString();
//
//            // Files
//            List<HardcodedIPLocationModel> hardcodedIPLocationModels = ipLocationModelService.findAll().stream()
//                    .filter(location -> {
//                        Set<ProjectModel> applicationsForFile = ProjectTraversalCache.getApplicationsForProject(context, location.getFile().getProjectModel());
//                        return applicationsForFile.contains(application);
//                    })
//                    .collect(Collectors.toList());
//
//            WindupVertexListModel<HardcodedIPLocationModel> hardcodedIPLocationModelWindupVertexListModel = new GraphService<>(context, WindupVertexListModel.class).create();
//            hardcodedIPLocationModelWindupVertexListModel.addAll(hardcodedIPLocationModels);
//
//            applicationHardcodedIpAddressesDto.files = StreamSupport.stream(hardcodedIPLocationModelWindupVertexListModel.spliterator(), false)
//                    .map(locationModel -> {
//                        ApplicationHardcodedIpAddressesDto.FileDto fileDto = new ApplicationHardcodedIpAddressesDto.FileDto();
//                        return fileDto;
//                    })
//                    .collect(Collectors.toList());
//
//            result.add(applicationHardcodedIpAddressesDto);
//        }
//
//        return result;
//    }
//
//    @Override
//    public Map<String, Object> getById(GraphRewrite event) {
//        return Collections.emptyMap();
//    }

}
