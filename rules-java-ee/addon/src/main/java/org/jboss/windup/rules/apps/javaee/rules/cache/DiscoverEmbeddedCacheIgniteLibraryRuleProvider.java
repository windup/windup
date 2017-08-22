package org.jboss.windup.rules.apps.javaee.rules.cache;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JarArchiveModel;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RuleMetadata(phase = InitialAnalysisPhase.class, perform = "Discover Apache Ignite libraries embedded")
public class DiscoverEmbeddedCacheIgniteLibraryRuleProvider extends AbstractRuleProvider
{

    @Override
    public Configuration getConfiguration(RuleLoaderContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        int ruleIDSuffix = 1;
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(JarArchiveModel.class)
                                .withProperty(FileModel.FILE_NAME, QueryPropertyComparisonType.REGEX, ".*ignite.*\\.jar$"))
                    .perform(
                                new AbstractIterationOperation<JarArchiveModel>()
                                {
                                    public void perform(GraphRewrite event, EvaluationContext context, JarArchiveModel fileResourceModel)
                                    {
                                        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                                        ClassificationModel classificationModel = classificationService.attachClassification(event, context,
                                                    fileResourceModel,
                                                    IssueCategoryRegistry.CLOUD_MANDATORY,
                                                    "Caching - Apache Ignite embedded library",
                                                    "The application embedds an Apache Ignite library.  \n"
                                                    + "\n"
                                                    + "Cloud readiness issue as potential state information that is not persisted to a backing service.");
                                        classificationModel.setEffort(5);

                                        TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                                        technologyTagService.addTagToFileModel(fileResourceModel, "Apache Ignite (embedded)",
                                                    TechnologyTagLevel.INFORMATIONAL);

                                    }
                                })
                    .withId(ruleIDPrefix + "_" + ruleIDSuffix++);
    }

}
