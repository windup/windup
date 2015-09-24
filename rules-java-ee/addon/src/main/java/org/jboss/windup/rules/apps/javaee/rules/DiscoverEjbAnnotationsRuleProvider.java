package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.forge.furnace.util.Strings;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.rules.apps.javaee.service.JmsDestinationService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with EJB related annotations, and adds EJB related metadata for these.
 */
public class DiscoverEjbAnnotationsRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverEjbAnnotationsRuleProvider.class);

    public DiscoverEjbAnnotationsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverEjbAnnotationsRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
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

    private String getAnnotationLiteralValue(JavaAnnotationTypeReferenceModel model, String name)
    {
        JavaAnnotationTypeValueModel valueModel = model.getAnnotationValues().get(name);

        if (valueModel instanceof JavaAnnotationLiteralTypeValueModel)
        {
            JavaAnnotationLiteralTypeValueModel literalTypeValue = (JavaAnnotationLiteralTypeValueModel) valueModel;
            return literalTypeValue.getLiteralValue();
        }
        else
        {
            return null;
        }
    }

    private void extractEJBMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel ejbClass = getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
        if (Strings.isNullOrEmpty(ejbName))
        {
            ejbName = ejbClass.getClassName();
        }

        String sessionType = javaTypeReference.getResolvedSourceSnippit()
                    .substring(javaTypeReference.getResolvedSourceSnippit().lastIndexOf(".") + 1);

        Service<EjbSessionBeanModel> sessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        EjbSessionBeanModel sessionBean = sessionBeanService.create();
        sessionBean.setBeanName(ejbName);
        sessionBean.setEjbClass(ejbClass);
        sessionBean.setSessionType(sessionType);
    }

    private void extractMessageDrivenMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        javaTypeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel ejbClass = getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
        if (Strings.isNullOrEmpty(ejbName))
        {
            ejbName = ejbClass.getClassName();
        }

        String destination = getAnnotationLiteralValue(annotationTypeReference, "mappedName");
        if (StringUtils.isBlank(destination))
        {
            JavaAnnotationTypeValueModel activationConfigAnnotation = annotationTypeReference.getAnnotationValues().get("activationConfig");
            destination = getDestinationFromActivationConfig(activationConfigAnnotation);
        }

        Service<EjbMessageDrivenModel> messageDrivenService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);
        EjbMessageDrivenModel messageDrivenBean = messageDrivenService.create();
        messageDrivenBean.setBeanName(ejbName);
        messageDrivenBean.setEjbClass(ejbClass);

        if (StringUtils.isNotBlank(destination))
        {
            JmsDestinationService jmsDestinationService = new JmsDestinationService(event.getGraphContext());
            messageDrivenBean.setDestination(jmsDestinationService.createUnique(destination));
        }

    }

    private String getDestinationFromActivationConfig(JavaAnnotationTypeValueModel annotationTypeReferenceModel)
    {

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
                String destination = getDestinationFromActivationConfig(javaAnnotationTypeReferenceModel);
                if (destination != null)
                {
                    return destination;
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
                if ("destination".equals(propertyName))
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

    private JavaClassModel getJavaClass(JavaTypeReferenceModel javaTypeReference)
    {
        JavaClassModel result = null;
        AbstractJavaSourceModel javaSource = javaTypeReference.getFile();
        for (JavaClassModel javaClassModel : javaSource.getJavaClasses())
        {
            // there can be only one public one, and the annotated class should be public
            if (javaClassModel.isPublic() != null && javaClassModel.isPublic())
            {
                result = javaClassModel;
                break;
            }
        }

        if (result == null)
        {
            // no public classes found, so try to find any class (even non-public ones)
            result = javaSource.getJavaClasses().iterator().next();
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}