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
import org.jboss.windup.rules.apps.javaee.service.RestWebServiceModelService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

@RuleMetadata(phase = InitialAnalysisPhase.class, after = AnalyzeJavaFilesRuleProvider.class)
public class DiscoverSpringRestServicesRuleProvider extends DiscoverAnnotatedClassRuleProvider
{
    private static final String ANNOTATIONS_ALIAS = "annotationsAlias";


    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        String ruleIDPrefix = getClass().getSimpleName();
        return ConfigurationBuilder
                .begin()
                .addRule()
                .when(JavaClass
                        .references("org.springframework.web.bind.annotation.{*}Mapping")
                        .at(TypeReferenceLocation.ANNOTATION)
                        .as(ANNOTATIONS_ALIAS))
                .perform(Iteration.over(ANNOTATIONS_ALIAS).perform(new AbstractIterationOperation<JavaTypeReferenceModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload)
                    {
                        extractMetadata(event, payload);
                    }
                }).endIteration())
                .withId(ruleIDPrefix + "_SpringAnnotatedRestRule");
    }

    protected void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference)
    {
        typeReference.getFile().setGenerateSourceReport(true);
        JavaAnnotationTypeReferenceModel javaAnnotationTypeReferenceModel = (JavaAnnotationTypeReferenceModel) typeReference;

        String path = getAnnotationLiteralValue(javaAnnotationTypeReferenceModel, "value");

        JavaClassModel implementationClass = new JavaClassService(event.getGraphContext()).getJavaClass(typeReference);

        RestWebServiceModelService service = new RestWebServiceModelService(event.getGraphContext());
        service.getOrCreate(typeReference.getFile().getApplication(), path, implementationClass).setSource("spring");
    }

    @Override
    public String toString()
    {
        return "DiscoverSpringRestnnotatedClasses";
    }
}