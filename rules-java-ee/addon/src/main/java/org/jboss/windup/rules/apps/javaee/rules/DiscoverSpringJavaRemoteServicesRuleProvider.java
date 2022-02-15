package org.jboss.windup.rules.apps.javaee.rules;

import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.parameters.ParameterizedIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.rules.apps.javaee.service.SpringRemoteServiceModelService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*
Rule to discover all Spring Remote services : RMI, Hessian, HTTP Invoker , JMS, AMQP, JaxWS that can be discovered
inside Java Classes
 */
@RuleMetadata(phase = MigrationRulesPhase.class, after = {DiscoverSpringBeanClassAnnotationsRuleProvider.class, DiscoverSpringConfigurationFilesRuleProvider.class, DiscoverSpringBeanMethodAnnotationsRuleProvider.class})
public class DiscoverSpringJavaRemoteServicesRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverSpringJavaRemoteServicesRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder
                .begin()
                .addRule().when(JavaClass.references("org.springframework.{exporterClass}.setService({serviceInterface})")
                        .at(TypeReferenceLocation.METHOD_CALL))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .where("exporterClass").matches("remoting.rmi.RmiServiceExporter|remoting.httpinvoker.HttpInvokerServiceExporter|remoting.caucho.HessianServiceExporter|remoting.jaxws.SimpleJaxWsServiceExporter|jms.remoting.JmsInvokerServiceExporter|amqp.remoting.service.AmqpInvokerServiceExporter")
                .where("serviceInterface").matches(".*")
                .withId(getClass().getSimpleName() + "_SpringJavaRemoteServicesRule");
    }

    private AbstractIterationOperation<JavaTypeReferenceModel> addSpringRMIBeanToGraph() {
        return new ParameterizedIterationOperation<JavaTypeReferenceModel>() {
            RegexParameterizedPatternBuilder exporterBuilder = new RegexParameterizedPatternBuilder("{exporterClass}");
            RegexParameterizedPatternBuilder argumentBuilder = new RegexParameterizedPatternBuilder("{serviceInterface}");

            @Override
            public Set<String> getRequiredParameterNames()
            {
                return Stream.concat(exporterBuilder.getRequiredParameterNames().stream(), argumentBuilder.getRequiredParameterNames().stream()).collect(Collectors.toSet());
            }

            @Override
            public void setParameterStore(ParameterStore store)
            {
                exporterBuilder.setParameterStore(store);
                argumentBuilder.setParameterStore(store);
            }

            @Override
            public void performParameterized(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel payload) {
                String exporterClass = exporterBuilder.build(event, context);
                String serviceInterface = argumentBuilder.build(event, context);
                extractMetadata(event,  payload, exporterClass, serviceInterface);
            }

        };
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference, String exporterClass, String serviceInterface) {
        try {
            JavaClassService javaClassService = new JavaClassService(event.getGraphContext());

            JavaClassModel exporterJavaClass = javaClassService.findAll().stream()
                    .filter(e->e.getQualifiedName().toLowerCase().contains(exporterClass.toLowerCase()))
                    .findAny().get();


            SpringBeanService springBeanService = new SpringBeanService(event.getGraphContext());

            JavaClassModel implementationClass = springBeanService
                    .findAll().stream()
                    .filter(e -> e.getJavaClass().getInterfaces().stream()
                            .anyMatch(o -> o.getQualifiedName().equalsIgnoreCase(serviceInterface)))
                    .findFirst().get().getJavaClass();

            JavaClassModel interfaceClass = javaClassService.findAll().stream()
                    .filter(e->e.getQualifiedName().equalsIgnoreCase(serviceInterface))
                    .findFirst().get();

            enableSourceReport(implementationClass);
            enableSourceReport(interfaceClass);

            SpringRemoteServiceModelService springRemoteRemoteServiceModelService = new SpringRemoteServiceModelService(event.getGraphContext());
            springRemoteRemoteServiceModelService.getOrCreate(typeReference.getFile().getApplication(), interfaceClass, implementationClass, exporterJavaClass);

            // Add the name to the Technological Tag Model, this will be used for Technologycal Usage Report
            TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
            String tagName = springRemoteRemoteServiceModelService.getTagName(exporterClass);
            technologyTagService.addTagToFileModel(interfaceClass.getClassFile(), tagName, TechnologyTagLevel.INFORMATIONAL);

        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }

    private void enableSourceReport(JavaClassModel javaClass) {
        if (javaClass.getOriginalSource() != null) {
            javaClass.getOriginalSource().setGenerateSourceReport(true);
        } else {
            javaClass.getDecompiledSource().setGenerateSourceReport(true);
        }
    }
}
