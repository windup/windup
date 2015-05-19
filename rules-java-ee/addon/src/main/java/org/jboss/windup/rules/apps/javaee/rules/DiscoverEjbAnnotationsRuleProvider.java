package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.model.resource.SourceFileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassFileModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationListTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
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

    private static final String ENTITY_ANNOTATIONS = "entityAnnotations";
    private static final String TABLE_ANNOTATIONS_LIST = "tableAnnotations";

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
                    .withId(ruleIDPrefix + "_MessageDrivenRule")
                    .addRule()
                    .when(JavaClass.references("javax.persistence.Entity").at(TypeReferenceLocation.ANNOTATION).as(ENTITY_ANNOTATIONS)
                                .or(JavaClass.references("javax.persistence.Table").at(TypeReferenceLocation.ANNOTATION).as(TABLE_ANNOTATIONS_LIST)))
                    .perform(Iteration.over(ENTITY_ANNOTATIONS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractEntityBeanMetadata(event, payload);
                        }
                    }).endIteration())
                    .withId(ruleIDPrefix + "_EntityBeanRule");

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
        ((SourceFileModel) javaTypeReference.getFile()).setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel ejbClass = getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
        String sessionType = javaTypeReference.getResolvedSourceSnippit()
                    .substring(javaTypeReference.getResolvedSourceSnippit().lastIndexOf(".") + 1);

        Service<EjbSessionBeanModel> sessionBeanService = new GraphService<>(event.getGraphContext(), EjbSessionBeanModel.class);
        EjbSessionBeanModel sessionBean = sessionBeanService.create();
        sessionBean.setBeanName(ejbName);
        sessionBean.setEjbClass(ejbClass);
        sessionBean.setSessionType(sessionType);
    }

    private void extractEntityBeanMetadata(GraphRewrite event, JavaTypeReferenceModel entityTypeReference)
    {
        ((SourceFileModel) entityTypeReference.getFile()).setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel entityAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) entityTypeReference;
        JavaAnnotationTypeReferenceModel tableAnnotationTypeReference = null;
        for (WindupVertexFrame annotationTypeReferenceBase : Variables.instance(event).findVariable(TABLE_ANNOTATIONS_LIST))
        {
            JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReferenceBase;
            if (annotationTypeReference.getFile().equals(entityTypeReference.getFile()))
            {
                tableAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) annotationTypeReference;
                break;
            }
        }

        JavaClassModel ejbClass = getJavaClass(entityTypeReference);

        String name = getAnnotationLiteralValue(entityAnnotationTypeReference, "name");
        if (name == null)
        {
            name = ejbClass.getClassName();
        }
        String tableName = tableAnnotationTypeReference == null ? name : getAnnotationLiteralValue(tableAnnotationTypeReference, "name");
        if (tableName == null)
        {
            tableName = name;
        }
        String persistenceType = "Container"; // It is always container in the case of Annotations

        Service<EjbEntityBeanModel> entityBeanService = new GraphService<>(event.getGraphContext(), EjbEntityBeanModel.class);
        EjbEntityBeanModel entityBean = entityBeanService.create();
        entityBean.setBeanName(name);
        entityBean.setEjbClass(ejbClass);
        entityBean.setTableName(tableName);
        entityBean.setPersistenceType(persistenceType);
    }

    private void extractMessageDrivenMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        ((SourceFileModel) javaTypeReference.getFile()).setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel ejbClass = getJavaClass(javaTypeReference);

        String ejbName = getAnnotationLiteralValue(annotationTypeReference, "name");
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
        FileModel originalFile = javaTypeReference.getFile();
        if (originalFile instanceof JavaSourceFileModel)
        {
            JavaSourceFileModel javaSource = (JavaSourceFileModel) originalFile;
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
        }
        else if (originalFile instanceof JavaClassFileModel)
        {
            result = ((JavaClassFileModel) originalFile).getJavaClass();
        }
        else
        {
            LOG.warning("Unrecognized file type with annotation found at: \"" + originalFile.getFilePath() + "\"");
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}