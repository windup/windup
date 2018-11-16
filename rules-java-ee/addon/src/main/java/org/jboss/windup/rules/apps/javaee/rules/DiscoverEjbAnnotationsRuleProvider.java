package org.jboss.windup.rules.apps.javaee.rules;


import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.model.ProjectModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.config.projecttraversal.ProjectTraversalCache;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.service.JmsDestinationService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.Set;

/**
 * Scans for classes with EJB related annotations, and adds EJB related metadata for these.
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverEjbAnnotationsRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {

        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(JavaClass.references("javax.ejb.{annotationType}").at(TypeReferenceLocation.ANNOTATION))
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractEJBMetadata(event, payload);
                        }
                    })
                    .where("annotationType").matches("Stateless|Stateful")
                    .withId(ruleIDPrefix + "_StatelessAndStatefulRule")
                    .addRule()
                    .when(JavaClass.references("javax.ejb.MessageDriven").at(TypeReferenceLocation.ANNOTATION))
                    .perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractMessageDrivenMetadata(event, payload);
                        }
                    })
                    .withId(ruleIDPrefix + "_MessageDrivenRule");
    }

    private void extractEJBMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        JavaClassModel ejbClass = javaClassService.getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
        if (Strings.isNullOrEmpty(ejbName))
        {
            ejbName = ejbClass.getClassName();
        }

        String sessionType = javaTypeReference.getResolvedSourceSnippit()
                    .substring(javaTypeReference.getResolvedSourceSnippit().lastIndexOf(".") + 1);

        Service<EjbSessionBeanModel> sessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        EjbSessionBeanModel sessionBean = sessionBeanService.create();

        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());
        sessionBean.setApplications(applications);
        sessionBean.setBeanName(ejbName);
        sessionBean.setEjbClass(ejbClass);
        sessionBean.setSessionType(sessionType);
    }

    private void extractMessageDrivenMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        JavaClassModel ejbClass = javaClassService.getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
        if (Strings.isNullOrEmpty(ejbName))
        {
            ejbName = ejbClass.getClassName();
        }

        JavaAnnotationTypeValueModel activationConfigAnnotation = annotationTypeReference.getAnnotationValues().get("activationConfig");

        String destination = getAnnotationLiteralValue(annotationTypeReference, "mappedName");
        if (StringUtils.isBlank(destination))
        {
            destination = getDestinationFromActivationConfig(activationConfigAnnotation);
        }

        Service<EjbMessageDrivenModel> messageDrivenService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);
        EjbMessageDrivenModel messageDrivenBean = messageDrivenService.create();
        Set<ProjectModel> applications = ProjectTraversalCache.getApplicationsForProject(event.getGraphContext(), javaTypeReference.getFile().getProjectModel());
        messageDrivenBean.setApplications(applications);
        messageDrivenBean.setBeanName(ejbName);
        messageDrivenBean.setEjbClass(ejbClass);

        if (StringUtils.isNotBlank(destination))
        {
            String destinationType = getPropertyFromActivationConfig(activationConfigAnnotation, "destinationType");

            JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());
            messageDrivenBean.setDestination(jmsDestinationService.createUnique(applications, destination, destinationType));
        }
    }

    private String getDestinationFromActivationConfig(JavaAnnotationTypeValueModel annotationTypeReferenceModel)
    {
        return this.getPropertyFromActivationConfig(annotationTypeReferenceModel, "destination");
    }

    private String getPropertyFromActivationConfig(JavaAnnotationTypeValueModel annotationTypeReferenceModel, String property)
    {
        if (property == null)
        {
            throw new IllegalArgumentException("property cannot be null");
        }

        if (annotationTypeReferenceModel == null)
        {
            return null;
        }

        if (annotationTypeReferenceModel instanceof JavaAnnotationListTypeValueModel)
        {
            for (JavaAnnotationTypeValueModel activationConfig : (JavaAnnotationListTypeValueModel) annotationTypeReferenceModel)
            {
                if (!(activationConfig instanceof JavaAnnotationTypeReferenceModel))
                {
                    continue;
                }

                JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = (JavaAnnotationTypeReferenceModel) activationConfig;
                String propertyValue = getPropertyFromActivationConfig(javaAnnotationTypeReferenceModel, property);
                if (propertyValue != null)
                {
                    return propertyValue;
                }
            }
            return null;
        }
        else if (annotationTypeReferenceModel instanceof JavaAnnotationTypeReferenceModel)
        {
            JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceModel;
            JavaAnnotationTypeValueModel propertyNameModel = javaAnnotationTypeReferenceModel.getAnnotationValues().get("propertyName");
            JavaAnnotationTypeValueModel propertyValueModel = javaAnnotationTypeReferenceModel.getAnnotationValues().get("propertyValue");

            if (propertyNameModel instanceof JavaAnnotationLiteralTypeValueModel
                        && propertyValueModel instanceof JavaAnnotationLiteralTypeValueModel)
            {
                String propertyName = ((JavaAnnotationLiteralTypeValueModel) propertyNameModel).getLiteralValue();
                String propertyValue = ((JavaAnnotationLiteralTypeValueModel) propertyValueModel).getLiteralValue();
                if (property.equals(propertyName))
                {
                    return propertyValue;
                }
            }
            return null;
        }
        else
        {
            return null;
        }
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}
