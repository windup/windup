package org.jboss.windup.rules.apps.javaee.rules;


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
import org.jboss.windup.rules.apps.java.condition.annotation.AnnotationTypeCondition;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
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
@RuleMetadata(phase = InitialAnalysisPhase.class, after = {AnalyzeJavaFilesRuleProvider.class, DiscoverSpringBeanClassAnnotationsRuleProvider.class})
public class DiscoverSpringBeanMethodAnnotationsRuleProvider extends AbstractRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {

        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("{*}({*})").at(TypeReferenceLocation.METHOD)
                            .annotationMatches(new AnnotationTypeCondition("org.springframework.context.annotation.Bean"))
                    )
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractAnnotationMetadata(event, payload);
                        }
                    })
                    .withId(ruleIDPrefix + "_SpringBeanMethodRule");
    }

    private void extractAnnotationMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference) {

        String methodReturnType = getReturnTypeFromMethodSnippit(javaTypeReference);
        getImplementationJavaClassModelFromInterface(event, methodReturnType)
                .ifPresent(javaImplementationClass -> {
                    enableSourceReport(javaImplementationClass);

                    // We add the info to the SpringBeanService
                    SpringBeanService sessionBeanService = new SpringBeanService(event.getGraphContext());
                    SpringBeanModel springBeanModel = sessionBeanService.create();

                    Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());
                    springBeanModel.setApplications(applications);
                    springBeanModel.setSpringBeanName(javaImplementationClass.getClassName());
                    springBeanModel.setJavaClass(javaImplementationClass);
                });
    }

    private Optional<JavaClassModel> getImplementationJavaClassModelFromInterface(GraphRewrite event, String returnType) {
        //with that interface we will search in the next lines the first class implementing that interface
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        Optional<JavaClassModel> returnTypeJavaClassModel = javaClassService.findAll().stream().filter(e -> e.getQualifiedName() != null && e.getQualifiedName().contains(returnType)).findFirst();
        if (returnTypeJavaClassModel != null && returnTypeJavaClassModel.isPresent() && returnTypeJavaClassModel.get().isInterface()) {
            return javaClassService.findAll().stream()
                    .filter(e -> e.getInterfaces() != null && e.getInterfaces().stream()
                            .anyMatch(intf -> intf.getQualifiedName() != null && intf.getQualifiedName().contains(returnType)))
                    .findAny();
        } else {
            return Optional.ofNullable(returnTypeJavaClassModel);
        }
    }

    @Override
    public String toString() {
        return "DiscoverSpringBeanAnnotatedClasses";
    }

    private void enableSourceReport(JavaClassModel javaClass) {
        if (javaClass.getOriginalSource() != null) {
            javaClass.getOriginalSource().setGenerateSourceReport(true);
        } else {
            javaClass.getDecompiledSource().setGenerateSourceReport(true);
        }
    }
}
