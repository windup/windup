package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
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
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class ApplicationServerResourcesRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "server-resources";

    @Override
    public String getBasePath() {
        return PATH;
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
            applicationServerResourcesDto.setApplicationId(application.getId().toString());
            applicationServerResourcesDto.setDatasources(new ArrayList<>());
            applicationServerResourcesDto.setJmsDestinations(new ArrayList<>());
            applicationServerResourcesDto.setJmsConnectionFactories(new ArrayList<>());
            applicationServerResourcesDto.setThreadPools(new ArrayList<>());
            applicationServerResourcesDto.setOtherJndiEntries(new ArrayList<>());

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
                            applicationServerResourcesDto.getDatasources().add(datasourceDto);

                            datasourceDto.setJndiLocation(dataSourceModel.getJndiLocation());
                            datasourceDto.setDatabaseTypeName(dataSourceModel.getDatabaseTypeName());
                            datasourceDto.setDatabaseTypeVersion(dataSourceModel.getDatabaseTypeVersion());

                            if (dataSourceModel instanceof LinkableModel) {
                                datasourceDto.setLinks(getLinks((LinkableModel) dataSourceModel));
                            }
                        } else if (jndiResourceModel instanceof JmsDestinationModel) {
                            JmsDestinationModel jmsDestinationModel = (JmsDestinationModel) jndiResourceModel;

                            ApplicationServerResourcesDto.JMSDestinationDto jmsDestinationDto = new ApplicationServerResourcesDto.JMSDestinationDto();
                            applicationServerResourcesDto.getJmsDestinations().add(jmsDestinationDto);

                            jmsDestinationDto.setJndiLocation(jmsDestinationModel.getJndiLocation());
                            ;
                            jmsDestinationDto.setDestinationType(jmsDestinationModel.getDestinationType() != null ? jmsDestinationModel.getDestinationType().toString() : null);

                            if (jmsDestinationModel instanceof LinkableModel) {
                                jmsDestinationDto.setLinks(getLinks((LinkableModel) jmsDestinationModel));
                            }
                        } else if (jndiResourceModel instanceof JmsConnectionFactoryModel) {
                            JmsConnectionFactoryModel jmsConnectionFactoryModel = (JmsConnectionFactoryModel) jndiResourceModel;

                            ApplicationServerResourcesDto.JMSConnectionFactoryDto jmsConnectionFactoryDto = new ApplicationServerResourcesDto.JMSConnectionFactoryDto();
                            applicationServerResourcesDto.getJmsConnectionFactories().add(jmsConnectionFactoryDto);

                            jmsConnectionFactoryDto.setJndiLocation(jmsConnectionFactoryModel.getJndiLocation());
                            jmsConnectionFactoryDto.setConnectionFactoryType(jmsConnectionFactoryModel.getConnectionFactoryType() != null ? jmsConnectionFactoryModel.getConnectionFactoryType().toString() : null);

                            if (jmsConnectionFactoryModel instanceof LinkableModel) {
                                jmsConnectionFactoryDto.setLinks(getLinks((LinkableModel) jmsConnectionFactoryModel));
                            }
                        } else {
                            ApplicationServerResourcesDto.OtherJndiEntryDto otherJndiEntryDto = new ApplicationServerResourcesDto.OtherJndiEntryDto();
                            applicationServerResourcesDto.getOtherJndiEntries().add(otherJndiEntryDto);

                            otherJndiEntryDto.setJndiLocation(jndiResourceModel.getJndiLocation());
                        }
                    });

            // Thread pools
            GraphService<WindupVertexListModel> threadPoolsListService = new GraphService<>(context, WindupVertexListModel.class);
            WindupVertexListModel<ThreadPoolModel> threadPoolsWindupVertexListModel = threadPoolsListService.create();
            threadPoolsWindupVertexListModel.addAll(threadPoolModelList);

            StreamSupport.stream(threadPoolsWindupVertexListModel.spliterator(), false)
                    .forEach(threadPoolModel -> {
                        ApplicationServerResourcesDto.ThreadPoolDto threadPoolDto = new ApplicationServerResourcesDto.ThreadPoolDto();
                        applicationServerResourcesDto.getThreadPools().add(threadPoolDto);

                        threadPoolDto.setPoolName(threadPoolModel.getPoolName());
                        threadPoolDto.setMinPoolSize(threadPoolModel.getMinPoolSize());
                        threadPoolDto.setMaxPoolSize(threadPoolModel.getMaxPoolSize());

                        if (threadPoolModel instanceof LinkableModel) {
                            threadPoolDto.setLinks(getLinks((LinkableModel) threadPoolModel));
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
                    linkDto.setLink(linkModel.getLink());
                    linkDto.setDescription(linkModel.getDescription());
                    return linkDto;
                })
                .collect(Collectors.toList());
    }

}
