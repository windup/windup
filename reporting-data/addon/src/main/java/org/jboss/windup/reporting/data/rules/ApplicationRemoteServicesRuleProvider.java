package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.PreReportPfRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationRemoteServicesDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
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
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = PreReportPfRenderingPhase.class,
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
                            jaxRsServiceDto.setInterfaceFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, jaxRSWebServiceModel.getImplementationClass().getQualifiedName()));
                        } else if (remoteServiceModel instanceof JaxWSWebServiceModel) {
                            JaxWSWebServiceModel jaxWSWebServiceModel = (JaxWSWebServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.JaxWsServiceDto jaxWsServiceDto = new ApplicationRemoteServicesDto.JaxWsServiceDto();
                            applicationRemoteServicesDto.getJaxWsServices().add(jaxWsServiceDto);

                            jaxWsServiceDto.setInterfaceName(jaxWSWebServiceModel.getInterface().getQualifiedName());
                            jaxWsServiceDto.setInterfaceFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, jaxWSWebServiceModel.getInterface().getQualifiedName()));

                            jaxWsServiceDto.setImplementationName(jaxWSWebServiceModel.getImplementationClass().getQualifiedName());
                            jaxWsServiceDto.setImplementationFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, jaxWSWebServiceModel.getImplementationClass().getQualifiedName()));
                        } else if (remoteServiceModel instanceof EjbRemoteServiceModel) {
                            EjbRemoteServiceModel ejbRemoteServiceModel = (EjbRemoteServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.EjbRemoteServiceDto ejbRemoteServiceDto = new ApplicationRemoteServicesDto.EjbRemoteServiceDto();
                            applicationRemoteServicesDto.getEjbRemoteServices().add(ejbRemoteServiceDto);

                            ejbRemoteServiceDto.setInterfaceName(ejbRemoteServiceModel.getInterface().getQualifiedName());
                            ejbRemoteServiceDto.setInterfaceFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, ejbRemoteServiceModel.getInterface().getQualifiedName()));

                            ejbRemoteServiceDto.setImplementationName(ejbRemoteServiceModel.getImplementationClass().getQualifiedName());
                            ejbRemoteServiceDto.setImplementationFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, ejbRemoteServiceModel.getImplementationClass().getQualifiedName()));
                        } else if (remoteServiceModel instanceof RMIServiceModel) {
                            RMIServiceModel rmiServiceModel = (RMIServiceModel) remoteServiceModel;

                            ApplicationRemoteServicesDto.RmiServiceDto rmiServiceDto = new ApplicationRemoteServicesDto.RmiServiceDto();
                            applicationRemoteServicesDto.getRmiServices().add(rmiServiceDto);

                            rmiServiceDto.setInterfaceName(rmiServiceModel.getInterface().getQualifiedName());
                            rmiServiceDto.setInterfaceFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, rmiServiceModel.getInterface().getQualifiedName()));

                            rmiServiceDto.setImplementationName(rmiServiceModel.getImplementationClass().getQualifiedName());
                            rmiServiceDto.setImplementationFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, rmiServiceModel.getImplementationClass().getQualifiedName()));
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
