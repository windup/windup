package org.jboss.windup.rules.apps.javaee.rules;

import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysis;
import org.jboss.windup.config.phase.RulePhase;
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
import org.jboss.windup.rules.apps.java.scan.ast.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.TypeReferenceLocation;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.model.EjbEntityBeanModel;
import org.jboss.windup.rules.apps.javaee.model.EjbMessageDrivenModel;
import org.jboss.windup.rules.apps.javaee.model.EjbSessionBeanModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with EJB related annotations, and adds EJB related metadata for these.
 */
public class DiscoverEjbAnnotationsRuleProvider extends WindupRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverEjbAnnotationsRuleProvider.class);

    private static final String ENTITY_ANNOTATIONS = "entityAnnotations";
    private static final String TABLE_ANNOTATIONS_LIST = "tableAnnotations";

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(AnalyzeJavaFilesRuleProvider.class);
    }

    @Override
    public Class<? extends RulePhase> getPhase()
    {
        return InitialAnalysis.class;
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
                        };
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

    private void extractEJBMetadata(GraphRewrite event, JavaTypeReferenceModel javaTypeReference)
    {
        ((SourceFileModel) javaTypeReference.getFile()).setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel annotationTypeReference = (JavaAnnotationTypeReferenceModel) javaTypeReference;

        JavaClassModel ejbClass = getJavaClass(javaTypeReference);

        String ejbName = annotationTypeReference.getAnnotationValues().get("name");
        String sessionType = javaTypeReference.getSourceSnippit().substring(javaTypeReference.getSourceSnippit().lastIndexOf(".") + 1);

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

        String name = entityAnnotationTypeReference.getAnnotationValues().get("name");
        if (name == null)
        {
            name = ejbClass.getClassName();
        }
        String tableName = tableAnnotationTypeReference == null ? name : tableAnnotationTypeReference.getAnnotationValues().get("name");
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

        String ejbName = annotationTypeReference.getAnnotationValues().get("name");
        String destination = annotationTypeReference.getAnnotationValues().get("mappedName");
        if (StringUtils.isBlank(destination))
        {
            String activationConfig = annotationTypeReference.getAnnotationValues().get("activationConfig");
            Pattern p = Pattern
                        .compile(".*propertyName[ \\t\\n]*=[ \\t\\n]*\"destination\"[ \\t\\n]*,[ \\t\\n]*propertyValue[ \\t\\n]*=[ \\t\\n]*\"(.*?)\".*");
            Matcher m = p.matcher(activationConfig);
            if (m.matches())
            {
                destination = m.group(1);
            }
        }

        Service<EjbMessageDrivenModel> messageDrivenService = new GraphService<>(event.getGraphContext(), EjbMessageDrivenModel.class);
        EjbMessageDrivenModel messageDrivenBean = messageDrivenService.create();
        messageDrivenBean.setBeanName(ejbName);
        messageDrivenBean.setEjbClass(ejbClass);
        messageDrivenBean.setDestination(destination);
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