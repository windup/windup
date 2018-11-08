package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.io.IOUtils;
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
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternBuilder;
import org.w3c.dom.Document;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.joox.JOOX.$;

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
                .addRule().when(JavaClass.references("org.springframework.remoting.{exporterClass}.setService({serviceInterface})")
                        .at(TypeReferenceLocation.METHOD_CALL))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .where("exporter").matches("rmi.RmiServiceExporter|http.HttpInvokerExporter")
                .where("argument").matches(".*")
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
                    .filter(e->e.getQualifiedName().equalsIgnoreCase(exporterClass))
                    .findAny().get();


            SpringBeanService springBeanService = new SpringBeanService(event.getGraphContext());
                JavaClassModel implementationClass = springBeanService
                        .findAll().stream()
                        .filter(e -> e.getJavaClass().getInterfaces().stream()
                                .anyMatch(o -> o.getQualifiedName().equalsIgnoreCase(serviceInterface)))
                        .findFirst().get().getJavaClass();

                JavaClassModel interfaceClass = javaClassService.findAll().stream()
                        .filter(e->e.getQualifiedName().equalsIgnoreCase(serviceInterface))
                        .findAny()
                        .orElse(null);

                enableSourceReport(implementationClass);
                enableSourceReport(interfaceClass);

                String tagName = getTagName(exporterClass);

                // Add the name to the Technological Tag Model, this will be used for Technologycal Usage Report
                TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                technologyTagService.addTagToFileModel(interfaceClass.getClassFile(), tagName, TechnologyTagLevel.INFORMATIONAL);

                SpringRemoteServiceModelService springRemoteRemoteServiceModelService = new SpringRemoteServiceModelService(event.getGraphContext());
                springRemoteRemoteServiceModelService.getOrCreate(typeReference.getFile().getApplication(), interfaceClass, exporterJavaClass);
        } catch (Exception e) {
            LOG.severe(e.getMessage());
        }
    }

    private String getTagName(String exporterClass) {
        if (exporterClass.contains("RmiServiceExporter")) {
            return "spring-rmi";
        } else if (exporterClass.contains("HttpInvokerServiceExporter")) {
            return "spring-httpinvoker";
        } else if (exporterClass.contains("HessianServiceExporter")) {
            return "spring-hessian";
        } else if (exporterClass.contains("JaxWsPortProxyFactoryBean")) {
            return "spring-jaxws";
        } else if (exporterClass.contains("JmsInvokerServiceExporter")) {
            return "spring-jms";
        } else if (exporterClass.contains("AmqpInvokerServiceExporter")) {
            return "spring-amqp";
        } else return "spring-undefined";
    }


    private void enableSourceReport(JavaClassModel implementationClass) {
        if (implementationClass.getOriginalSource() != null) {
            implementationClass.getOriginalSource().setGenerateSourceReport(true);
        } else {
            implementationClass.getDecompiledSource().setGenerateSourceReport(true);
        }
    }

    private String getImplementationClass(String implementationBean, Document wholeDocument) {
        return $(wholeDocument).xpath("//bean[@id=\"" + implementationBean + "\"]").first().attr("class");
    }

    private String getImplementationBean(Document xmlDocSnippet) {
        return $(xmlDocSnippet).xpath("//property[@name=\"service\"]").first().attr("ref");
    }

    private String getInterfaceName(Document xmlDocSnippet) {
        return $(xmlDocSnippet).xpath("//property[@name=\"serviceInterface\"]").first().attr("value");
    }

    private String getExporterClass(Document xmlDocSnippet) {
        return $(xmlDocSnippet).first().attr("class");
    }

}
