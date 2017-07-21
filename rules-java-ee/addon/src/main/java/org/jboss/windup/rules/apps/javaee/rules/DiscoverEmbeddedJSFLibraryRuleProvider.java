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

@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover Java libraries embedded")
public class DiscoverEmbeddedJSFLibraryRuleProvider extends AbstractRuleProvider
{

    @Override
    public Configuration getConfiguration(RuleLoaderContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        int ruleIDSuffix = 1;
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(JarArchiveModel.class)
                                .withProperty(FileModel.FILE_NAME, QueryPropertyComparisonType.REGEX, ".*jsf-[(api)|(impl)].*\\.jar$"))
                    .perform(
                                new AbstractIterationOperation<JarArchiveModel>()
                                {
                                    public void perform(GraphRewrite event, EvaluationContext context, JarArchiveModel fileResourceModel)
                                    {
                                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                        ClassificationModel classificationModel = classificationService.attachClassification(event, context,
                                                    fileResourceModel,
                                                    IssueCategoryRegistry.MANDATORY,
                                                    "JSF embedded library",
                                                    "The application has a JSF library embedded. "
                                                                + "Red Hat JBoss EAP includes JSF as a module with a version that has been tested and is supported by Red Hat.\n"
                                                                + "There are two options for using the JSF library:  \n"
                                                                + "\n"
                                                                + "1. Keep it embedded as it is now. This approach is low effort but the application will not use a tested and supported library.  \n"
                                                                + "2. Switch to use the JSF library in the EAP module. This will require effort to remove the embedded library and configure the application to use the module's library, but then the application will rely on a tested and supported version of the JSF library.  \n"
                                                                + "\n"
                                                                + "In the links below there are instructions to enable alternative versions for both EAP 6 and 7.");
                                        classificationModel.setEffort(3);
                                        GraphContext graphContext = event.getGraphContext();
                                        LinkService linkService = new LinkService(graphContext);
                                        LinkModel componentDetailsLink = linkService.create();
                                        componentDetailsLink.setDescription("Red Hat JBoss EAP: Component Details");
                                        componentDetailsLink.setLink("https://access.redhat.com/articles/112673");
                                        classificationService.attachLink(classificationModel, componentDetailsLink);
                                        LinkModel documentationEAP6Link = linkService.create();
                                        documentationEAP6Link.setDescription("Red Hat JBoss EAP 6: JavaServer Faces (JSF) Code Changes");
                                        documentationEAP6Link.setLink(
                                                    "https://access.redhat.com/documentation/en-US/JBoss_Enterprise_Application_Platform/6.4/html/Migration_Guide/sect-Changes_Dependent_on_Your_Application_Architecture_and_Components.html#sect-JSF_changes");
                                        classificationService.attachLink(classificationModel, documentationEAP6Link);
                                        LinkModel jsf12WithEAP6Link = linkService.create();
                                        jsf12WithEAP6Link.setDescription("How to use JSF 1.2 with EAP 6");
                                        jsf12WithEAP6Link.setLink("https://access.redhat.com/solutions/690953");
                                        classificationService.attachLink(classificationModel, jsf12WithEAP6Link);
                                        LinkModel documentationEAP7Link = linkService.create();
                                        documentationEAP7Link.setDescription("Red Hat JBoss EAP 7: JavaServer Faces (JSF) Code Changes");
                                        documentationEAP7Link.setLink(
                                                    "https://access.redhat.com/documentation/en-us/red_hat_jboss_enterprise_application_platform/7.0/html/migration_guide/application_migration_changes#migrate_jsf_code_changes");
                                        classificationService.attachLink(classificationModel, documentationEAP7Link);
                                        LinkModel jsf12WithEAP7Link = linkService.create();
                                        jsf12WithEAP7Link.setDescription("How to use JSF 1.2 with EAP 7?");
                                        jsf12WithEAP7Link.setLink("https://access.redhat.com/solutions/2773121");
                                        classificationService.attachLink(classificationModel, jsf12WithEAP7Link);

                                        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                                        technologyTagService.addTagToFileModel(fileResourceModel, "Embedded JAR library",
                                                    TechnologyTagLevel.INFORMATIONAL);

                                    }
                                })
                    .withId(ruleIDPrefix + "_" + ruleIDSuffix++);
    }

}
