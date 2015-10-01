package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.javaee.service.JaxRSWebServiceModelService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JAX-RS related annotations, and adds JAX-RS related metadata for these.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverJaxRsAnnotationsRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverJaxRsAnnotationsRuleProvider.class);

    private static final String JAXRS_ANNOTATIONS = "jaxrsAnnotations";

    public DiscoverJaxRsAnnotationsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverJaxRsAnnotationsRuleProvider.class)
                    .setPhase(InitialAnalysisPhase.class)
                    .addExecuteAfter(AnalyzeJavaFilesRuleProvider.class));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass
                                .references("javax.ws.rs.Path")
                                .at(TypeReferenceLocation.ANNOTATION)
                                .as(JAXRS_ANNOTATIONS))
                    .perform(Iteration.over(JAXRS_ANNOTATIONS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractMetadata(event, payload);
                        }
                    }).endIteration())
                    .withId(ruleIDPrefix + "_JAXRSAnnotationRule");
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference)
    {
        typeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel jaxRSAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) typeReference;

        String path = getAnnotationLiteralValue(jaxRSAnnotationTypeReference, "value");
        JavaClassModel implementationClass = getJavaClass(typeReference);

        JaxRSWebServiceModelService service = new JaxRSWebServiceModelService(event.getGraphContext());
        service.getOrCreate(path, implementationClass);
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