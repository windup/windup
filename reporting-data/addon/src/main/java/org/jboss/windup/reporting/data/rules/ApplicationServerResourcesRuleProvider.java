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
import org.jboss.windup.reporting.data.dto.ApplicationServerResourcesDto;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.model.association.LinkableModel;
import org.jboss.windup.rules.apps.javaee.model.DataSourceModel;
import org.jboss.windup.rules.apps.javaee.model.JNDIResourceModel;
import org.jboss.windup.rules.apps.javaee.model.JmsConnectionFactoryModel;
import org.jboss.windup.rules.apps.javaee.model.JmsDestinationModel;
import org.jboss.windup.rules.apps.javaee.model.ThreadPoolModel;
import org.jboss.windup.rules.apps.javaee.service.JNDIResourceService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class ApplicationServerResourcesRuleProvider extends AbstractApiRuleProvider {

    @Override
    public String getBasePath() {
        return "server-resources";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        GraphContext context = event.getGraphContext();
        WindupConfigurationModel configurationModel = WindupConfigurationService.getConfigurationModel(context);
        JNDIResourceService jndiResourceService = new JNDIResourceService(context);
        GraphService<ThreadPoolModel> threadPoolService = new GraphService<>(context, ThreadPoolModel.class);

        List<ApplicationServerResourcesDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();

            ApplicationServerResourcesDto applicationServerResourcesDto = new ApplicationServerResourcesDto();
            applicationServerResourcesDto.applicationId = application.getId().toString();
            applicationServerResourcesDto.datasources = new ArrayList<>();
            applicationServerResourcesDto.jmsDestinations = new ArrayList<>();
            applicationServerResourcesDto.jmsConnectionFactories = new ArrayList<>();
            applicationServerResourcesDto.threadPools = new ArrayList<>();
            applicationServerResourcesDto.otherJndiEntries = new ArrayList<>();

            List<JNDIResourceModel> jndiResourceModelList = jndiResourceService.findAll().stream()
                    .filter(jndiResourceModel -> jndiResourceModel.isAssociatedWithApplication(application))
                    .collect(Collectors.toList());
            List<ThreadPoolModel> threadPoolModelList = threadPoolService.findAll().stream()
                    .filter(threadPoolModel -> threadPoolModel.getApplications().contains(application))
                    .collect(Collectors.toList());

            // JNDI resources
            GraphService<WindupVertexListModel> jndiListService = new GraphService<>(context, WindupVertexListModel.class);
            WindupVertexListModel<JNDIResourceModel> jndiWindupVertexListModel = jndiListService.create();
            jndiWindupVertexListModel.addAll(jndiResourceModelList);

            StreamSupport.stream(jndiWindupVertexListModel.spliterator(), false)
                    .forEach(jndiResourceModel -> {
                        if (jndiResourceModel instanceof DataSourceModel) {
                            DataSourceModel dataSourceModel = (DataSourceModel) jndiResourceModel;

                            ApplicationServerResourcesDto.DatasourceDto datasourceDto = new ApplicationServerResourcesDto.DatasourceDto();
                            applicationServerResourcesDto.datasources.add(datasourceDto);

                            datasourceDto.jndiLocation = dataSourceModel.getJndiLocation();
                            datasourceDto.databaseTypeName = dataSourceModel.getDatabaseTypeName();
                            datasourceDto.databaseTypeVersion = dataSourceModel.getDatabaseTypeVersion();

                            if (dataSourceModel instanceof LinkableModel) {
                                datasourceDto.links = getLinks((LinkableModel) dataSourceModel);
                            }
                        } else if (jndiResourceModel instanceof JmsDestinationModel) {
                            JmsDestinationModel jmsDestinationModel = (JmsDestinationModel) jndiResourceModel;

                            ApplicationServerResourcesDto.JMSDestinationDto jmsDestinationDto = new ApplicationServerResourcesDto.JMSDestinationDto();
                            applicationServerResourcesDto.jmsDestinations.add(jmsDestinationDto);

                            jmsDestinationDto.jndiLocation = jmsDestinationModel.getJndiLocation();
                            jmsDestinationDto.destinationType = jmsDestinationModel.getDestinationType() != null ? jmsDestinationModel.getDestinationType().toString() : null;

                            if (jmsDestinationModel instanceof LinkableModel) {
                                jmsDestinationDto.links = getLinks((LinkableModel) jmsDestinationModel);
                            }
                        } else if (jndiResourceModel instanceof JmsConnectionFactoryModel) {
                            JmsConnectionFactoryModel jmsConnectionFactoryModel = (JmsConnectionFactoryModel) jndiResourceModel;

                            ApplicationServerResourcesDto.JMSConnectionFactoryDto jmsConnectionFactoryDto = new ApplicationServerResourcesDto.JMSConnectionFactoryDto();
                            applicationServerResourcesDto.jmsConnectionFactories.add(jmsConnectionFactoryDto);

                            jmsConnectionFactoryDto.jndiLocation = jmsConnectionFactoryModel.getJndiLocation();
                            jmsConnectionFactoryDto.connectionFactoryType = jmsConnectionFactoryModel.getConnectionFactoryType() != null ? jmsConnectionFactoryModel.getConnectionFactoryType().toString() : null;

                            if (jmsConnectionFactoryModel instanceof LinkableModel) {
                                jmsConnectionFactoryDto.links = getLinks((LinkableModel) jmsConnectionFactoryModel);
                            }
                        } else {
                            ApplicationServerResourcesDto.OtherJndiEntryDto otherJndiEntryDto = new ApplicationServerResourcesDto.OtherJndiEntryDto();
                            applicationServerResourcesDto.otherJndiEntries.add(otherJndiEntryDto);

                            otherJndiEntryDto.jndiLocation = jndiResourceModel.getJndiLocation();
                        }
                    });

            // Thread pools
            GraphService<WindupVertexListModel> threadPoolsListService = new GraphService<>(context, WindupVertexListModel.class);
            WindupVertexListModel<ThreadPoolModel> threadPoolsWindupVertexListModel = threadPoolsListService.create();
            threadPoolsWindupVertexListModel.addAll(threadPoolModelList);

            StreamSupport.stream(threadPoolsWindupVertexListModel.spliterator(), false)
                    .forEach(threadPoolModel -> {
                        ApplicationServerResourcesDto.ThreadPoolDto threadPoolDto = new ApplicationServerResourcesDto.ThreadPoolDto();
                        applicationServerResourcesDto.threadPools.add(threadPoolDto);

                        threadPoolDto.poolName = threadPoolModel.getPoolName();
                        threadPoolDto.minPoolSize = threadPoolModel.getMinPoolSize();
                        threadPoolDto.maxPoolSize = threadPoolModel.getMaxPoolSize();

                        if (threadPoolModel instanceof LinkableModel) {
                            threadPoolDto.links = getLinks((LinkableModel) threadPoolModel);
                        }
                    });

            result.add(applicationServerResourcesDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

    private List<ApplicationServerResourcesDto.LinkDto> getLinks(LinkableModel linkableModel) {
        return linkableModel.getLinks().stream().map(linkModel -> {
                    ApplicationServerResourcesDto.LinkDto linkDto = new ApplicationServerResourcesDto.LinkDto();
                    linkDto.link = linkModel.getLink();
                    linkDto.description = linkModel.getDescription();
                    return linkDto;
                })
                .collect(Collectors.toList());
    }

}
