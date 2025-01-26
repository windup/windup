package org.jboss.windup.rules.apps.java.scan.provider;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.metadata.Technology;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.DiscoverProjectStructurePhase;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryBuilderWith;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.FileService;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.model.TechnologyTagModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.condition.Dependency;
import org.jboss.windup.rules.apps.java.condition.Version;
import org.jboss.windup.rules.apps.java.model.project.MavenProjectModel;
import org.jboss.windup.rules.apps.xml.DiscoverXmlFilesRuleProvider;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.rules.files.condition.FileContent;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;


@RuleMetadata(phase = InitialAnalysisPhase.class, after = DiscoverMavenProjectsRuleProvider.class,
        sourceTechnologies = {
                @Technology(id = "springboot")
        },
        targetTechnologies = {
                @Technology(id = "rhr")
        })
public class DiscoverNonProductizedSpringBootRuleProvider extends IteratingRuleProvider<MavenProjectModel> {
    private static final String TECH_TAG = "Spring Boot";
    private static final TechnologyTagLevel TECH_TAG_LEVEL = TechnologyTagLevel.INFORMATIONAL;

    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(MavenProjectModel.class).withProperty(MavenProjectModel.ARTIFACT_ID,
                        QueryPropertyComparisonType.CONTAINS_TOKEN,"spring-boot")
                .andNot(Query.fromType(MavenProjectModel.class).withProperty(MavenProjectModel.VERSION,
                        QueryPropertyComparisonType.CONTAINS_TOKEN, "redhat"));
    }

    public void perform(GraphRewrite event, EvaluationContext context, MavenProjectModel payload)
    {

            addToReports(event, context, payload);

    }

    @Override
    public String toStringPerform()
    {
        return "Discover Non-Productized versions of Spring Boot artifacts";
    }


    private void addToReports(GraphRewrite event, EvaluationContext context, MavenProjectModel mvnModel) {
        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());

        mvnModel.getFileModelsNoDirectories().forEach(fileModel ->
        {
            TechnologyTagModel technologyTag = technologyTagService.addTagToFileModel(fileModel, TECH_TAG, TECH_TAG_LEVEL);
            classificationService.attachClassification(event, context, fileModel, IssueCategoryRegistry.INFORMATION,
                    "Spring Boot Productization", "Non Productized Spring Boot version");
            FileService fileService = new FileService(event.getGraphContext());
            fileService.addTypeToModel(fileModel);
        });

    }


}
