package org.jboss.windup.rules.apps.javaee.rules;


import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.service.JaxRSWebServiceModelService;
import org.jboss.windup.rules.apps.javaee.service.SpringRestWebServiceModelService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JAX-RS related annotations, and adds JAX-RS related metadata for these.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverJaxRsAnnotationsRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    private static final String JAXRS_ANNOTATIONS = "jaxrsAnnotations";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
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

    protected void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference)
    {
        typeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel jaxRSAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) typeReference;

        String path = getAnnotationLiteralValue(jaxRSAnnotationTypeReference, "value");
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
        JavaClassModel implementationClass = javaClassService.getJavaClass(typeReference);

        JaxRSWebServiceModelService service = new JaxRSWebServiceModelService(event.getGraphContext());
        service.getOrCreate(typeReference.getFile().getApplication(), path, implementationClass);
    }

     @Override
    public String toString()
    {
        return "DiscoverJaxRSAnnotatedClasses";
    }
}