package org.jboss.windup.rules.apps.javaee.rules;


import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Optional;
import java.util.Set;

/**
 * Scans for classes with Spring bean related annotations, and adds Bean related metadata for these.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverSpringBeanClassAnnotationsRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {

        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("org.springframework.stereotype.{annotationType}").at(TypeReferenceLocation.ANNOTATION))
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractAnnotationMetadata(event, payload);
                        }
                    })
                    .where("annotationType").matches("Component|Controller|Service|Repository")
                    .withId(ruleIDPrefix + "_SpringBeanRule");
    }

    private void extractAnnotationMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference) {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        Optional<JavaClassModel> javaClass = javaTypeReference.getFile()
                    .getJavaClasses()
                    .stream()
                    .filter(JavaClassModel::isPublic)
                    .findAny();
        if (javaClass.isPresent()) {
            String beanName = getAnnotationLiteralValue(annotationTypeReference, "name");
            if (Strings.isNullOrEmpty(beanName)) {
                beanName = javaClass.get().getClassName();
            }

            SpringBeanService sessionBeanService = new SpringBeanService(event.getGraphContext());
            SpringBeanModel springBeanModel = sessionBeanService.create();

            Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());
            springBeanModel.setApplications(applications);
            springBeanModel.setSpringBeanName(beanName);
            springBeanModel.setJavaClass(javaClass.get());
        }
    }

    @Override
    public String toString() {
        return "DiscoverSpringBeanAnnotatedClasses";
    }
}
