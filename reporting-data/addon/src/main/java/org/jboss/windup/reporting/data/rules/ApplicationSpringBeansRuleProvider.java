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
import org.jboss.windup.reporting.data.dto.ApplicationSpringBeansDto;
import org.jboss.windup.reporting.data.rules.utils.DataUtils;
import org.jboss.windup.reporting.model.WindupVertexListModel;
import org.jboss.windup.reporting.service.SourceReportService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.model.SpringConfigurationFileModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;

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
public class ApplicationSpringBeansRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "spring-beans";

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

        List<ApplicationSpringBeansDto> result = new ArrayList<>();

        for (FileModel inputPath : configurationModel.getInputPaths()) {
            ProjectModel application = inputPath.getProjectModel();
            SpringBeanService springBeanService = new SpringBeanService(context);

            ApplicationSpringBeansDto applicationSpringBeansDto = new ApplicationSpringBeansDto();
            applicationSpringBeansDto.setApplicationId(application.getId().toString());

            Iterable<SpringBeanModel> models = springBeanService.findAllByApplication(application);
            if (!models.iterator().hasNext()) {
                applicationSpringBeansDto.setBeans(Collections.emptyList());
                continue;
            }
            GraphService<WindupVertexListModel> listService = new GraphService<>(context, WindupVertexListModel.class);
            WindupVertexListModel<SpringBeanModel> springBeanList = listService.create();
            springBeanList.addAll(models);

            applicationSpringBeansDto.setBeans(StreamSupport.stream(springBeanList.spliterator(), false)
                    .map(springBeanModel -> {
                        ApplicationSpringBeansDto.SpringBeanDto springBeanDto = new ApplicationSpringBeansDto.SpringBeanDto();
                        springBeanDto.setBeanName(springBeanModel.getSpringBeanName());
                        springBeanDto.setClassName(springBeanModel.getJavaClass().getQualifiedName());

                        SpringConfigurationFileModel springConfiguration = springBeanModel.getSpringConfiguration();
                        if (springConfiguration != null) {
                            // If beanName could not be identified try to extract it from SpringConfiguration
                            if (springBeanDto.getBeanName() == null) {
                                springBeanDto.setBeanName(springConfiguration.getPrettyPathWithinProject());
                            }

                            springBeanDto.setBeanDescriptorFileId(DataUtils.getSourceFileId(sourceReportService, springConfiguration));
                        }

                        JavaClassModel clz = springBeanModel.getJavaClass();
                        if (clz != null) {
                            springBeanDto.setClassFileId(DataUtils.getSourceFileId(javaClassService, sourceReportService, clz.getQualifiedName()));
                        }

                        return springBeanDto;
                    })
                    .collect(Collectors.toList())
            );

            result.add(applicationSpringBeansDto);
        }

        return result;
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }

}
