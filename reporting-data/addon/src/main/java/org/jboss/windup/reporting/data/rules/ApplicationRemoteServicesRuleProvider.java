package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationRemoteServicesDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.EjbRemoteServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxRSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RemoteServiceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationRemoteServicesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "remote-services";

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        SourceReportService sourceReportService = new SourceReportService(context);
        JavaClassService javaClassService = new JavaClassService(context);
        GraphService<RemoteServiceModel> remoteServices = new GraphService<>(context, RemoteServiceModel.class);

        List<ApplicationRemoteServicesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationRemoteServicesDto applicationRemoteServicesDto = new ApplicationRemoteServicesDto();
            applicationRemoteServicesDto.setApplicationId(application.getId().toString());
            applicationRemoteServicesDto.setJaxRsServices(new ArrayList<>());
            applicationRemoteServicesDto.setJaxWsServices(new ArrayList<>());
            applicationRemoteServicesDto.setEjbRemoteServices(new ArrayList<>());
            applicationRemoteServicesDto.setRmiServices(new ArrayList<>());

            List<RemoteServiceModel> remoteServiceModelList = remoteServices.findAll().stream()
                    .filter(remoteServiceModel -> remoteServiceModel.isAssociatedWithApplication(application))
                    .collect(Collectors.toList());

            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
            WindupVertexListModel<RemoteServiceModel> windupVertexListModel = listService.create();
            windupVertexListModel.addAll(remoteServiceModelList);

            StreamSupport.stream(windupVertexListModel.spliterator(), false)
                    .forEach(remoteServiceModel -> {
                        if (remoteServiceModel instanceof JaxRSWebServiceModel) {
                            JaxRSWebServiceModel jaxRSWebServiceModel = (JaxRSWebServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.JaxRsServiceDto jaxRsServiceDto = new ApplicationRemoteServicesDto.JaxRsServiceDto();
                            applicationRemoteServicesDto.getJaxRsServices().add(jaxRsServiceDto);

                            jaxRsServiceDto.setPath(jaxRSWebServiceModel.getPath());
                            jaxRsServiceDto.setInterfaceName(jaxRSWebServiceModel.getImplementationClass().getQualifiedName());
                            jaxRsServiceDto.setInterfaceFileId(StreamSupport.stream(javaClassService.getJavaSource(jaxRSWebServiceModel.getImplementationClass().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );
                        } else if (remoteServiceModel instanceof JaxWSWebServiceModel) {
                            JaxWSWebServiceModel jaxWSWebServiceModel = (JaxWSWebServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.JaxWsServiceDto jaxWsServiceDto = new ApplicationRemoteServicesDto.JaxWsServiceDto();
                            applicationRemoteServicesDto.getJaxWsServices().add(jaxWsServiceDto);

                            jaxWsServiceDto.setInterfaceName(jaxWSWebServiceModel.getInterface().getQualifiedName());
                            jaxWsServiceDto.setInterfaceFileId(StreamSupport.stream(javaClassService.getJavaSource(jaxWSWebServiceModel.getInterface().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );

                            jaxWsServiceDto.setImplementationName(jaxWSWebServiceModel.getImplementationClass().getQualifiedName());
                            jaxWsServiceDto.setImplementationFileId(StreamSupport.stream(javaClassService.getJavaSource(jaxWSWebServiceModel.getImplementationClass().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );
                        } else if (remoteServiceModel instanceof EjbRemoteServiceModel) {
                            EjbRemoteServiceModel ejbRemoteServiceModel = (EjbRemoteServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.EjbRemoteServiceDto ejbRemoteServiceDto = new ApplicationRemoteServicesDto.EjbRemoteServiceDto();
                            applicationRemoteServicesDto.getEjbRemoteServices().add(ejbRemoteServiceDto);

                            ejbRemoteServiceDto.setInterfaceName(ejbRemoteServiceModel.getInterface().getQualifiedName());
                            ejbRemoteServiceDto.setInterfaceFileId(StreamSupport.stream(javaClassService.getJavaSource(ejbRemoteServiceModel.getInterface().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );

                            ejbRemoteServiceDto.setImplementationName(ejbRemoteServiceModel.getImplementationClass().getQualifiedName());
                            ejbRemoteServiceDto.setImplementationFileId(StreamSupport.stream(javaClassService.getJavaSource(ejbRemoteServiceModel.getImplementationClass().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );
                        } else if (remoteServiceModel instanceof RMIServiceModel) {
                            RMIServiceModel rmiServiceModel = (RMIServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.RmiServiceDto rmiServiceDto = new ApplicationRemoteServicesDto.RmiServiceDto();
                            applicationRemoteServicesDto.getRmiServices().add(rmiServiceDto);

                            rmiServiceDto.setInterfaceName(rmiServiceModel.getInterface().getQualifiedName());
                            rmiServiceDto.setInterfaceFileId(StreamSupport.stream(javaClassService.getJavaSource(rmiServiceModel.getInterface().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );

                            rmiServiceDto.setImplementationName(rmiServiceModel.getImplementationClass().getQualifiedName());
                            rmiServiceDto.setImplementationFileId(StreamSupport.stream(javaClassService.getJavaSource(rmiServiceModel.getImplementationClass().getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null)
                            );
                        }
                    });

            result.add(applicationRemoteServicesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
