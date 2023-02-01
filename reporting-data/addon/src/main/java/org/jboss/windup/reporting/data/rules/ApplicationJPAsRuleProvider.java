package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPf4RenderingPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.WindupConfigurationModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.data.dto.ApplicationJPAsDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.JPAConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.model.JPAEntityModel;
import org.jboss.windup.rules.apps.javaee.model.JPANamedQueryModel;
import org.jboss.windup.rules.apps.javaee.service.JPAConfigurationFileService;
import org.jboss.windup.rules.apps.javaee.service.JPAEntityService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportPf4RenderingPhase.class,
        haltOnException = true
)
public class ApplicationJPAsRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "jpa";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);

        JPAConfigurationFileService jpaConfigurationFileService = new JPAConfigurationFileService(context);
        JPAEntityService jpaEntityService = new JPAEntityService(context);
        GraphService<JPANamedQueryModel> jpaNamedQueryService = new GraphService<>(context, JPANamedQueryModel.class);

        SourceReportService sourceReportService = new SourceReportService(context);
        JavaClassService javaClassService = new JavaClassService(context);

        List<ApplicationJPAsDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationJPAsDto applicationJPAsDto = new ApplicationJPAsDto();
            applicationJPAsDto.applicationId = application.getId().toString();
            applicationJPAsDto.entities = new ArrayList<>();
            applicationJPAsDto.namesQueries = new ArrayList<>();
            applicationJPAsDto.jpaConfigurations = new ArrayList<>();

            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);

            // JPA Configurations
            List<JPAConfigurationFileModel> jpaConfigurationFileModelList = jpaConfigurationFileService.findAll().stream()
                    .filter(jpaConfigurationFileModel -> {
                        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(context, jpaConfigurationFileModel.getProjectModel());
                        return applications.contains(application);
                    })
                    .collect(Collectors.toList());

            WindupVertexListModel<JPAConfigurationFileModel> jpaConfigurationWindupVertexListModel = listService.create();
            jpaConfigurationWindupVertexListModel.addAll(jpaConfigurationFileModelList);

            StreamSupport.stream(jpaConfigurationWindupVertexListModel.spliterator(), false)
                    .forEach(jpaConfigurationFileModel -> {
                        ApplicationJPAsDto.JPAConfigurationDto jpaConfigurationDto = new ApplicationJPAsDto.JPAConfigurationDto();
                        applicationJPAsDto.jpaConfigurations.add(jpaConfigurationDto);

                        jpaConfigurationDto.path = jpaConfigurationFileModel.getPrettyPath();
                        jpaConfigurationDto.version = jpaConfigurationFileModel.getSpecificationVersion();
                        jpaConfigurationDto.persistentUnits = jpaConfigurationFileModel.getPersistenceUnits().stream()
                                .map(persistenceUnitModel -> {
                                    ApplicationJPAsDto.PersistentUnitDto persistentUnitDto = new ApplicationJPAsDto.PersistentUnitDto();
                                    persistentUnitDto.name = persistenceUnitModel.getName();
                                    persistentUnitDto.properties = new HashMap<>(persistenceUnitModel.getProperties());
                                    persistentUnitDto.datasources = persistenceUnitModel.getDataSources().stream()
                                            .map(dataSourceModel -> {
                                                ApplicationJPAsDto.DatasourceDto datasourceDto = new ApplicationJPAsDto.DatasourceDto();
                                                datasourceDto.jndiLocation = dataSourceModel.getJndiLocation();
                                                datasourceDto.databaseTypeName = dataSourceModel.getDatabaseTypeName();
                                                datasourceDto.isXA = Objects.equals(dataSourceModel.getXa(), true);
                                                return datasourceDto;
                                            })
                                            .collect(Collectors.toList());
                                    return persistentUnitDto;
                                })
                                .collect(Collectors.toList());
                    });

            // Entities
            List<JPAEntityModel> jpaEntityModelList = jpaEntityService.findAll().stream()
                    .filter(jpaEntityModel -> jpaEntityModel.getApplications().contains(application))
                    .collect(Collectors.toList());

            WindupVertexListModel<JPAEntityModel> jpaEntityWindupVertexListModel = listService.create();
            jpaEntityWindupVertexListModel.addAll(jpaEntityModelList);

            StreamSupport.stream(jpaEntityWindupVertexListModel.spliterator(), false)
                    .forEach(jpaEntityModel -> {
                        ApplicationJPAsDto.JPAEntityDto jpaEntityDto = new ApplicationJPAsDto.JPAEntityDto();
                        applicationJPAsDto.entities.add(jpaEntityDto);

                        jpaEntityDto.entityName = jpaEntityModel.getEntityName();
                        jpaEntityDto.tableName = jpaEntityModel.getTableName();

                        JavaClassModel clz = jpaEntityModel.getJavaClass();
                        if (clz != null) {
                            jpaEntityDto.className = clz.getQualifiedName();
                            jpaEntityDto.classFileId = StreamSupport.stream(javaClassService.getJavaSource(clz.getQualifiedName()).spliterator(), false)
                                    .map(sourceReportService::getSourceReportForFileModel)
                                    .filter(Objects::nonNull)
                                    .map(f -> f.getSourceFileModel().getId().toString())
                                    .findFirst()
                                    .orElse(null);
                        }
                    });

            // Named queries
            List<JPANamedQueryModel> jpaNamedQueryModelList = jpaNamedQueryService.findAll().stream()
                    .filter(jpaNamedQueryModel -> jpaNamedQueryModel.getJpaEntity().getApplications().contains(application))
                    .collect(Collectors.toList());

            WindupVertexListModel<JPANamedQueryModel> jpaNamedQueryWindupVertexListModel = listService.create();
            jpaNamedQueryWindupVertexListModel.addAll(jpaNamedQueryModelList);

            StreamSupport.stream(jpaNamedQueryWindupVertexListModel.spliterator(), false)
                    .forEach(jpaNamedQueryModel -> {
                        ApplicationJPAsDto.JPANamedQueryDto jpaNamedQueryDto = new ApplicationJPAsDto.JPANamedQueryDto();
                        applicationJPAsDto.namesQueries.add(jpaNamedQueryDto);

                        jpaNamedQueryDto.queryName = jpaNamedQueryModel.getQueryName();
                        jpaNamedQueryDto.query = jpaNamedQueryModel.getQuery();
                    });

            result.add(applicationJPAsDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }


}
