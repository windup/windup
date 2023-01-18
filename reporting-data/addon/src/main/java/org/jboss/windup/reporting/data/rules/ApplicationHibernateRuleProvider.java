package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationHibernateDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.HibernateConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.HibernateEntityModel;
import org.jboss.windup.rules.apps.javaee.service.HibernateConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.HibernateEntityService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class ApplicationHibernateRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "hibernate";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);

        HibernateConfigurationFileService hibernateConfigurationFileService = new HibernateConfigurationFileService(context);
        HibernateEntityService hibernateEntityService = new HibernateEntityService(context);

        SourceReportService sourceReportService = new SourceReportService(context);
        JavaClassService javaClassService = new JavaClassService(context);

        List<ApplicationHibernateDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationHibernateDto applicationHibernateDto = new ApplicationHibernateDto();
            applicationHibernateDto.applicationId = application.getId().toString();
            applicationHibernateDto.entities = new ArrayList<>();
            applicationHibernateDto.hibernateConfigurations = new ArrayList<>();

            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

            // Hibernate Configurations
            Iterable<HibernateConfigurationFileModel> hibernateConfigurationFilesByApplication = hibernateConfigurationFileService.findAllByApplication(application);
            List<HibernateConfigurationFileModel> hibernateConfigurationFileModelList = StreamSupport.stream(hibernateConfigurationFilesByApplication.spliterator(), false)
                    .collect(Collectors.toList());

            WindupVertexListModel<HibernateConfigurationFileModel> hibernateConfigurationWindupVertexListModel = listService.create();
            hibernateConfigurationWindupVertexListModel.addAll(hibernateConfigurationFileModelList);

            StreamSupport.stream(hibernateConfigurationWindupVertexListModel.spliterator(), false)
                    .forEach(jpaConfigurationFileModel -> {
                        ApplicationHibernateDto.HibernateConfigurationDto hibernateConfigurationDto = new ApplicationHibernateDto.HibernateConfigurationDto();
                        applicationHibernateDto.hibernateConfigurations.add(hibernateConfigurationDto);

                        hibernateConfigurationDto.path = jpaConfigurationFileModel.getPrettyPath();
                        hibernateConfigurationDto.sessionFactories = jpaConfigurationFileModel.getHibernateSessionFactories().stream()
                                .map(sessionFactoryModel -> {
                                    ApplicationHibernateDto.HibernateSessionFactoryDto hibernateSessionFactoryDto = new ApplicationHibernateDto.HibernateSessionFactoryDto();
                                    hibernateSessionFactoryDto.properties = new HashMap<>(sessionFactoryModel.getSessionFactoryProperties());
                                    return hibernateSessionFactoryDto;
                                })
                                .collect(Collectors.toList());
                    });

            // Entities
            Iterable<HibernateEntityModel> hibernateEntitiesByApplication = hibernateEntityService.findAllByApplication(application);
            List<HibernateEntityModel> hibernateEntitiesModelList = StreamSupport.stream(hibernateEntitiesByApplication.spliterator(), false)
                    .collect(Collectors.toList());

            WindupVertexListModel<HibernateEntityModel> hibernateEntityWindupVertexListModel = listService.create();
            hibernateEntityWindupVertexListModel.addAll(hibernateEntitiesModelList);

            StreamSupport.stream(hibernateEntityWindupVertexListModel.spliterator(), false)
                    .forEach(hibernateEntityModel -> {
                        ApplicationHibernateDto.HibernateEntityDto hibernateEntityDto = new ApplicationHibernateDto.HibernateEntityDto();
                        applicationHibernateDto.entities.add(hibernateEntityDto);

                        hibernateEntityDto.tableName = hibernateEntityModel.getTableName();

                        JavaClassModel clz = hibernateEntityModel.getJavaClass();
                        if (clz != null) {
                            hibernateEntityDto.className = clz.getQualifiedName();
                            hibernateEntityDto.classFileId = StreamSupport.stream(javaClassService.getJavaSource(clz.getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null);
                        }
                    });

            result.add(applicationHibernateDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
