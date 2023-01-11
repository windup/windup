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
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class ApplicationBeansRuleProvider extends AbstractApiRuleProvider {

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
            applicationEJBsDto.beans = new ArrayList<>();

            for (EjbBeanBaseModel beanBaseModel : ejbService.findAll()) {
                if (!Iterables.contains(beanBaseModel.getApplications(), application)) {
                    continue;
                }

                ApplicationEJBsDto.BeanDto bean = new ApplicationEJBsDto.BeanDto();
                applicationEJBsDto.beans.add(bean);

                bean.beanName = beanBaseModel.getBeanName();
                bean.className = beanBaseModel.getEjbClass().getQualifiedName();

                JavaClassModel clz = beanBaseModel.getEjbClass();
                if (clz != null) {
                    bean.classFileId = StreamSupport.stream(javaClassService.getJavaSource(clz.getQualifiedName()).spliterator(), false)
                            .map(sourceReportService::getSourceReportForFileModel)
                            .filter(Objects::nonNull)
                            .map(f -> f.getSourceFileModel().getId().toString())
                            .findFirst()
                            .orElse(null);
                }

                if (beanBaseModel instanceof EjbMessageDrivenModel) {
                    bean.type = ApplicationEJBsDto.BeanType.MESSAGE_DRIVEN_BEAN;
                    EjbMessageDrivenModel mdb = (EjbMessageDrivenModel) beanBaseModel;

                    bean.beanDescriptorFileId = getDescriptorFileId(sourceReportService, mdb.getEjbDeploymentDescriptor());
                    bean.jmsDestination = mdb.getDestination().getJndiLocation();
                } else if (beanBaseModel instanceof EjbEntityBeanModel) {
                    bean.type = ApplicationEJBsDto.BeanType.ENTITY_BEAN;
                    EjbEntityBeanModel entity = (EjbEntityBeanModel) beanBaseModel;

                    bean.beanDescriptorFileId = getDescriptorFileId(sourceReportService, entity.getEjbDeploymentDescriptor());
                    bean.tableName = entity.getTableName();
                    bean.persistenceType = entity.getPersistenceType();
                } else if (beanBaseModel instanceof EjbSessionBeanModel) {
                    if ("stateful".equalsIgnoreCase(beanBaseModel.getSessionType())) {
                        bean.type = ApplicationEJBsDto.BeanType.STATEFUL_SESSION_BEAN;
                    } else {
                        bean.type = ApplicationEJBsDto.BeanType.STATELESS_SESSION_BEAN;
                    }
                    EjbSessionBeanModel sessionBean = (EjbSessionBeanModel) beanBaseModel;

                    bean.beanDescriptorFileId = getDescriptorFileId(sourceReportService, sessionBean.getEjbDeploymentDescriptor());

                    JavaClassModel ejbHome = sessionBean.getEjbHome();
                    if (ejbHome != null) {
                        bean.homeEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(ejbHome.getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }

                    JavaClassModel ejbLocal = sessionBean.getEjbLocal();
                    if (ejbLocal != null) {
                        bean.localEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(ejbLocal.getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }

                    JavaClassModel ejbRemote = sessionBean.getEjbRemote();
                    if (ejbRemote != null) {
                        bean.remoteEJBFileId = StreamSupport.stream(javaClassService.getJavaSource(ejbRemote.getQualifiedName()).spliterator(), false)
                                .map(sourceReportService::getSourceReportForFileModel)
                                .filter(Objects::nonNull)
                                .map(f -> f.getSourceFileModel().getId().toString())
                                .findFirst()
                                .orElse(null);
                    }


                    bean.jndiLocations = Stream.of(
                                    sessionBean.getGlobalJndiReference() != null ? sessionBean.getGlobalJndiReference().getJndiLocation() : null,
                                    sessionBean.getModuleJndiReference() != null ? sessionBean.getModuleJndiReference().getJndiLocation() : null,
                                    sessionBean.getLocalJndiReference() != null ? sessionBean.getLocalJndiReference().getJndiLocation() : null
                            )
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
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
