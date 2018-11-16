package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.service.JaxWSWebServiceModelService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.logging.Logger;

/**
 * Scans for classes with JAX-WS related annotations, and adds JAX-WS related metadata for these.
 *
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverJaxWSAnnotationsRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    private static final Logger LOG = Logging.get(DiscoverJaxWSAnnotationsRuleProvider.class);

    private static final String JAXWS_ANNOTATIONS = "jaxwsAnnotations";

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                    .begin()
                    .addRule()
                    .when(JavaClass
                                .references("javax.jws.WebService")
                                .at(TypeReferenceLocation.ANNOTATION)
                                .as(JAXWS_ANNOTATIONS))
                    .perform(Iteration.over(JAXWS_ANNOTATIONS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                    {
                        @Override
                        public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                        {
                            extractMetadata(event, payload);
                        }
                    }).endIteration())
                    .withId(ruleIDPrefix + "_JAXWSAnnotationRule");
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference)
    {
        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());

        JavaClassModel implementationClass = javaClassService.getJavaClass(typeReference);

        // first, find out if it implements an interface.
        // TODO: handle the interface only case, where clients exist but no implementation
        if (!implementationClass.getInterfaces().iterator().hasNext())
        {
            return;
        }

        LOG.info("Processing: " + typeReference);

        typeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel jaxWsAnnotationTypeReference = (JavaAnnotationTypeReferenceModel) typeReference;

        String endpointInterfaceQualifiedName = getAnnotationLiteralValue(jaxWsAnnotationTypeReference, "endpointInterface");
        JavaClassModel endpointInterface = null;
        if (StringUtils.isNotBlank(endpointInterfaceQualifiedName))
        {
            endpointInterface = javaClassService.getOrCreatePhantom(endpointInterfaceQualifiedName);
            for (AbstractJavaSourceModel source : javaClassService.getJavaSource(endpointInterface.getQualifiedName()))
                source.setGenerateSourceReport(true);
        }

        JaxWSWebServiceModelService service = new JaxWSWebServiceModelService(event.getGraphContext());
        service.getOrCreate(typeReference.getFile().getApplication(), endpointInterface, implementationClass);
    }


    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}
