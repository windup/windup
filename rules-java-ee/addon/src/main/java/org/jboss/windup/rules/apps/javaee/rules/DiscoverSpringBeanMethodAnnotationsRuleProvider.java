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

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private boolean couldBeMethodReturnType(String element) {

      if ("public static final private protected".contains(element.trim().toLowerCase())) {
          return false;
      } else {
          // we want elements without any non word character
          return containsNonWordCharacters(element);
      }
    }

    private boolean containsNonWordCharacters(String element) {
        Pattern p = Pattern.compile("[^\\w\\s]");
        Matcher m = p.matcher(element);
        return !m.find();
    }

    private void extractAnnotationMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference) {
        javaTypeReference.getFile().setGenerateSourceReport(true);

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
        //with that interface we will seach in the next lines the first class implementing that interface
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        Optional<JavaClassModel> returnTypeJavaClassModel = javaClassService.findAll().stream().filter(e -> e.getQualifiedName().contains(returnType)).findFirst();
        if (returnTypeJavaClassModel.isPresent() && returnTypeJavaClassModel.get().isInterface()) {
            return javaClassService.findAll().stream()
                    .filter(e -> e.getInterfaces()
                            .stream()
                            .anyMatch(intf -> intf.getQualifiedName().contains(returnType)))
                    .findAny();
        } else {
            return returnTypeJavaClassModel;
        }
    }

    private String getReturnTypeFromMethodSnippit(JavaTypeReferenceModel javaTypeReference) {
        //get the type returned by the method---> Bean Interface
        return Arrays.stream(javaTypeReference.getSourceSnippit()
                .split(" "))
                .filter(this::couldBeMethodReturnType)
                .findFirst()
                .get();
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
