package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.LinkModel;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.LinkService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover Seam Java libraries embedded")
public class DiscoverEmbeddedSeam2LibraryRuleProvider extends AbstractRuleProvider
{

    @Override
    public Configuration getConfiguration(RuleLoaderContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        int ruleIDSuffix = 1;
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(JarArchiveModel.class)
                                .withProperty(FileModel.FILE_NAME, QueryPropertyComparisonType.REGEX, "jboss-seam.*\\.jar$"))
                    .perform(
                                new AbstractIterationOperation<JarArchiveModel>()
                                {
                                    public void perform(GraphRewrite event, EvaluationContext context, JarArchiveModel fileResourceModel)
                                    {
                                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                        ClassificationModel classificationModel = classificationService.attachClassification(event, context,
                                                    fileResourceModel,
                                                    IssueCategoryRegistry.MANDATORY,
                                                    "Seam 2 embedded library",
                                                    "The application has a Seam library embedded.  \n"
                                                    +"While official support for Seam 2.2 applications was dropped in JBoss EAP 6, it was still possible to configure dependencies for JSF 1.2 and Hibernate 3 to allow Seam 2.2 applications to run on that release. \n"
                                                    +"Seam 2.3 should work on JBoss EAP 6 even some framework features and integrations from Seam 2.2 are not supported. \n"
                                                    +"\n"
                                                    +"Red Hat JBoss EAP 7, which now includes JSF 2.2 and Hibernate 5, does not support Seam 2.2 or Seam 2.3 due to end of life of Red Hat JBoss Web Framework Kit. It is recommended that you rewrite your Seam components using CDI beans. \n"
                                                    + "\n"
                                                    + "In the links below there are the instructions to enable alternatives for both EAP 6 and 7.");
                                        classificationModel.setEffort(5);
                                        GraphContext graphContext = event.getGraphContext();
                                        LinkService linkService = new LinkService(graphContext);
                                        LinkModel seam22DetailsLink = linkService.create();
                                        seam22DetailsLink.setDescription("EAP 6 - Migrate Seam 2.2 applications");
                                        seam22DetailsLink.setLink("https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html-single/Migration_Guide/index.html#sect-Migrate_Seam_2.2_Applications");
                                        classificationService.attachLink(classificationModel, seam22DetailsLink);

                                        LinkModel seam23migrEAP6Link = linkService.create();
                                        seam23migrEAP6Link.setDescription("Red Hat JBoss EAP 6: Migration from 2.2 to 2.3");
                                        seam23migrEAP6Link.setLink(
                                                    "https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_Web_Framework_Kit/2.7/html-single/Seam_Guide/index.html#migration23");
                                        classificationService.attachLink(classificationModel, seam23migrEAP6Link);
                                        
                                        LinkModel documentationWFKLink = linkService.create();
                                        documentationWFKLink.setDescription("Red Hat JBoss EAP: Migration from Seam 2 to Java EE and alternatives");
                                        documentationWFKLink.setLink(
                                                    "https://access.redhat.com/documentation/en-US/Red_Hat_JBoss_Web_Framework_Kit/2.7/html-single/Seam_Guide/index.html#idm54350960");
                                        classificationService.attachLink(classificationModel, documentationWFKLink);
                                        
                                        LinkModel jsf12WithEAP7Link = linkService.create();
                                        jsf12WithEAP7Link.setDescription("How to use JSF 1.2 with EAP 7?");
                                        jsf12WithEAP7Link.setLink("https://access.redhat.com/solutions/2773121");
                                        classificationService.attachLink(classificationModel, jsf12WithEAP7Link);

                                        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                                        technologyTagService.addTagToFileModel(fileResourceModel, "Seam embedded JAR library",
                                                    TechnologyTagLevel.INFORMATIONAL);

                                    }
                                })
                    .withId(ruleIDPrefix + "_" + ruleIDSuffix++);
    }

}
