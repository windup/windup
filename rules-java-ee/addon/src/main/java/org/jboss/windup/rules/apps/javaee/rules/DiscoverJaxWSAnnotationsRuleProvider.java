package org.jboss.windup.rules.apps.javaee.rules;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
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
import org.jboss.windup.rules.apps.java.model.JavaSourceFileModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationLiteralTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeReferenceModel;
import org.jboss.windup.rules.apps.java.scan.ast.annotations.JavaAnnotationTypeValueModel;
import org.jboss.windup.rules.apps.java.scan.provider.AnalyzeJavaFilesRuleProvider;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.service.JaxWSWebServiceModelService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Scans for classes with JAX-WS related annotations, and adds JAX-WS related metadata for these.
 * 
 * @author <a href="mailto:bradsdavis@gmail.com">Brad Davis</a>
 */
public class DiscoverJaxWSAnnotationsRuleProvider extends AbstractRuleProvider
{
    private static Logger LOG = Logging.get(DiscoverJaxWSAnnotationsRuleProvider.class);

    private static final String JAXWS_ANNOTATIONS = "jaxwsAnnotations";

    public DiscoverJaxWSAnnotationsRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverJaxWSAnnotationsRuleProvider.class)
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

        JavaClassModel implementationClass = getJavaClass(typeReference);

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
        JavaClassModel endpointInterface = javaClassService.getOrCreatePhantom(endpointInterfaceQualifiedName);
        if (StringUtils.isNotBlank(endpointInterfaceQualifiedName))
        {
            for (JavaSourceFileModel source : javaClassService.getJavaSource(endpointInterface.getQualifiedName()))
                source.setGenerateSourceReport(true);
        }

        JaxWSWebServiceModelService service = new JaxWSWebServiceModelService(event.getGraphContext());
        service.getOrCreate(endpointInterface, implementationClass);
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

    @Override
    public String toString()
    {
        return "DiscoverEJBAnnotatedClasses";
    }
}