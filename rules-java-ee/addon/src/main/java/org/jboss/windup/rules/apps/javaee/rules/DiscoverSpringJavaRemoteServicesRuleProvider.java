package org.jboss.windup.rules.apps.javaee.rules;

import com.sun.jmx.remote.internal.RMIExporter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.windup.ast.java.data.TypeReferenceLocation;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.service.SpringRemoteServiceModelService;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Logger;

import static org.joox.JOOX.$;

/*
Rule to discover all Spring Remote services : RMI, Hessian, HTTP Invoker , JMS, AMQP, JaxWS that can be discovered
inside Java Classes
 */
@RuleMetadata(phase = MigrationRulesPhase.class)
public class DiscoverSpringJavaRemoteServicesRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverSpringJavaRemoteServicesRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder
                .begin()
                .addRule().when(JavaClass.references("org.springframework.remoting.{*}.setService({argument})").at(TypeReferenceLocation.METHOD_CALL))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .where("argument").matches(".*")
                .withId(getClass().getSimpleName() + "_SpringJavaRemoteServicesRule");
    }

    private AbstractIterationOperation<JavaTypeReferenceModel> addSpringRMIBeanToGraph() {
        return new AbstractIterationOperation<JavaTypeReferenceModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, JavaTypeReferenceModel typeReference) {
                extractMetadata(event,  typeReference);
            }
        };
    }

    private void extractMetadata(GraphRewrite event, JavaTypeReferenceModel typeReference) {
        try {
            LOG.info("testing");
            String javaSource = IOUtils.toString(typeReference.getFile().asInputStream(), "UTF-8");
            int methodBodyStart = javaSource.indexOf(typeReference.getSourceSnippit()) + typeReference.getSourceSnippit().length();
            int startSetServiceInterface = javaSource.indexOf("setServiceInterface(", methodBodyStart);
            int endSetServiceInterface = javaSource.indexOf(")", startSetServiceInterface);
            int startSetService = javaSource.indexOf("setService(", methodBodyStart);
            int endSetService = javaSource.indexOf(")", startSetService);
            String serviceInterface = javaSource.substring(startSetServiceInterface + "setServiceInterface(".length(), endSetServiceInterface);
            String service = javaSource.substring(startSetService + "setService(".length(), endSetService);
            LOG.info("testing end");




//            String interfaceName = getInterfaceName(xmlDocSnippet);
//            String implementationBean = getImplementationBean(xmlDocSnippet);
//
//            if (!StringUtils.isEmpty(interfaceName) && (!StringUtils.isEmpty(implementationBean))) {
//                // we obtain the Whole XML Document to find the implementation Bean
//                Document wholeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(typeReference.getFile().asInputStream());
//                String implementationClass = getImplementationClass(implementationBean, wholeDocument);
//
//                JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
//                JavaClassModel interfaceJavaClassModel = javaClassService.getByName(interfaceName);
//                JavaClassModel implementationJavaClassModel = javaClassService.getByName(implementationClass);
//                JavaClassModel exporterInterfaceClassModel = javaClassService.getByName(exporterClass);
//
//                // Create the "source code" report for the Service Interface
//                enableSourceReport(interfaceJavaClassModel);
//
//                String tagName = getTagName(exporterClass);
//
//                // Add the name to the Technological Tag Model, this will be used for Technologycal Usage Report
//                TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
//                technologyTagService.addTagToFileModel(interfaceJavaClassModel.getClassFile(), tagName, TechnologyTagLevel.INFORMATIONAL);
//
//                SpringRemoteServiceModelService springRemoteRemoteServiceModelService = new SpringRemoteServiceModelService(event.getGraphContext());
//                springRemoteRemoteServiceModelService.getOrCreate(typeReference.getFile().getApplication(), interfaceJavaClassModel, exporterInterfaceClassModel);
//
//                // Create the "source code" report for the Implementation.
//                enableSourceReport(implementationJavaClassModel);
//            }
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
