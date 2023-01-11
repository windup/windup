package org.jboss.windup.reporting.data.rules;

import com.google.common.collect.Iterables;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationEJBsDto;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.EjbBeanBaseModel;
import org.jboss.windup.rules.apps.javaee.model.EjbDeploymentDescriptorModel;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.service.EjbBeanService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class ApplicationEJBsRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "ejb";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        SourceReportService sourceReportService = new SourceReportService(context);
        JavaClassService javaClassService = new JavaClassService(context);

        List<ApplicationEJBsDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();
            EjbBeanService ejbService = new EjbBeanService(context);

            ApplicationEJBsDto applicationEJBsDto = new ApplicationEJBsDto();
            applicationEJBsDto.applicationId = application.getId().toString();
            applicationEJBsDto.entityBeans = new ArrayList<>();
            applicationEJBsDto.sessionBeans = new ArrayList<>();
            applicationEJBsDto.messageDrivenBeans = new ArrayList<>();

            for (EjbBeanBaseModel beanBaseModel : ejbService.findAll()) {
                if (!Iterables.contains(beanBaseModel.getApplications(), application)) {
                    continue;
                }

                Consumer<ApplicationEJBsDto.BeanDto> setCommonBeanData = beanDto -> {
                    beanDto.beanName = beanBaseModel.getBeanName();
                    beanDto.className = beanBaseModel.getEjbClass().getQualifiedName();

                    JavaClassModel clz = beanBaseModel.getEjbClass();
                    if (clz != null) {
                        beanDto.classFileId = StreamSupport.stream(javaClassService.getJavaSource(clz.getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }
                };

                if (beanBaseModel instanceof EjbMessageDrivenModel) {
                    ApplicationEJBsDto.MessageDrivenBeanDto beanDto = new ApplicationEJBsDto.MessageDrivenBeanDto();
                    applicationEJBsDto.messageDrivenBeans.add(beanDto);
                    setCommonBeanData.accept(beanDto);

                    EjbMessageDrivenModel mdb = (EjbMessageDrivenModel) beanBaseModel;
                    beanDto.beanDescriptorFileId = getDescriptorFileId(sourceReportService, mdb.getEjbDeploymentDescriptor());
                    beanDto.jmsDestination = mdb.getDestination().getJndiLocation();
                } else if (beanBaseModel instanceof EjbEntityBeanModel) {
                    ApplicationEJBsDto.EntityBeanDto beanDto = new ApplicationEJBsDto.EntityBeanDto();
                    applicationEJBsDto.entityBeans.add(beanDto);
                    setCommonBeanData.accept(beanDto);

                    EjbEntityBeanModel entity = (EjbEntityBeanModel) beanBaseModel;
                    beanDto.beanDescriptorFileId = getDescriptorFileId(sourceReportService, entity.getEjbDeploymentDescriptor());
                    beanDto.tableName = entity.getTableName();
                    beanDto.persistenceType = entity.getPersistenceType();
                } else if (beanBaseModel instanceof EjbSessionBeanModel) {
                    ApplicationEJBsDto.SessionBeanDto beanDto = new ApplicationEJBsDto.SessionBeanDto();
                    applicationEJBsDto.sessionBeans.add(beanDto);
                    setCommonBeanData.accept(beanDto);

                    EjbSessionBeanModel sessionBean = (EjbSessionBeanModel) beanBaseModel;
                    beanDto.beanDescriptorFileId = getDescriptorFileId(sourceReportService, sessionBean.getEjbDeploymentDescriptor());
                    beanDto.type = "stateful".equalsIgnoreCase(beanBaseModel.getSessionType()) ? ApplicationEJBsDto.SessionBeanType.STATEFUL : ApplicationEJBsDto.SessionBeanType.STATELESS;

                    if (sessionBean.getEjbHome() != null) {
                        beanDto.homeEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(sessionBean.getEjbHome().getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }
                    if (sessionBean.getEjbLocal() != null) {
                        beanDto.localEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(sessionBean.getEjbLocal().getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }
                    if (sessionBean.getEjbRemote() != null) {
                        beanDto.remoteEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(sessionBean.getEjbRemote().getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }

                    beanDto.jndiLocation = Stream.of(
                                    sessionBean.getGlobalJndiReference() != null ? sessionBean.getGlobalJndiReference().getJndiLocation() : null,
                                    sessionBean.getModuleJndiReference() != null ? sessionBean.getModuleJndiReference().getJndiLocation() : null,
                                    sessionBean.getLocalJndiReference() != null ? sessionBean.getLocalJndiReference().getJndiLocation() : null
                            )
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse(null);
                }
            }

            result.add(applicationEJBsDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private String getDescriptorFileId(SourceReportService sourceReportService, EjbDeploymentDescriptorModel ejbDeploymentDescriptor) {
        if (ejbDeploymentDescriptor != null) {
            return sourceReportService.getSourceReportForFileModel(ejbDeploymentDescriptor)
                    .getSourceFileModel()
                    .getId()
                    .toString();
        }
        return null;
    }

}
