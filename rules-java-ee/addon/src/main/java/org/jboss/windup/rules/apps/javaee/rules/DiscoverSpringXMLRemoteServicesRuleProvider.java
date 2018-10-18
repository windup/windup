package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.model.TechnologyTagLevel;
import org.jboss.windup.reporting.service.TechnologyTagService;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.SpringBeanModel;
import org.jboss.windup.rules.apps.javaee.service.JaxWSWebServiceModelService;
import org.jboss.windup.rules.apps.javaee.service.SpringBeanService;
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
import java.util.stream.StreamSupport;

import static org.joox.JOOX.$;
import static org.joox.JOOX.none;

/*
Rule to discover all Spring Remote services : RMI, Hessian, HTTP Invoker , JMS, AMQP, JaxWS that can be discovered
using the ".remoting" package pattern in the class for the exporter, in XML files
 */
@RuleMetadata(phase = MigrationRulesPhase.class, after = DiscoverSpringConfigurationFilesRuleProvider.class)
public class DiscoverSpringXMLRemoteServicesRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverSpringXMLRemoteServicesRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder
                .begin()
                .addRule(getXMLBeanRule());
    }

    private Rule getXMLBeanRule() {
        return RuleBuilder.define()
                .when(XmlFile.matchesXpath("//s:bean[starts-with(@class,'org.springframework') and contains(@class,'.remoting')]").namespace("s", "http://www.springframework.org/schema/beans"))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .withId(getClass().getSimpleName() + "_SpringXMLRemoteServicesRule");
    }

    private AbstractIterationOperation<XmlTypeReferenceModel> addSpringRMIBeanToGraph() {
        return new AbstractIterationOperation<XmlTypeReferenceModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlTypeReferenceModel typeReference) {
                extractMetadata(event,  typeReference);
            }
        };
    }

    private void extractMetadata(GraphRewrite event, XmlTypeReferenceModel typeReference) {


        try {
            // we obtain the XML fragment with the Spring Exporter Bean
            Document xmlDocSnippet = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(typeReference.getSourceSnippit())));

            String exporterClass = getExporterClass(xmlDocSnippet);
            String interfaceName = getInterfaceName(xmlDocSnippet);
            String implementationBean = getImplementationBean(xmlDocSnippet);

            if (!StringUtils.isEmpty(interfaceName) && (!StringUtils.isEmpty(implementationBean))) {
                // we obtain the Whole XML Document to find the implementation Bean
                JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                JavaClassModel implementationJavaClassModel = getImplementationClass(implementationBean, event.getGraphContext());

                JavaClassModel interfaceJavaClassModel = javaClassService.getByName(interfaceName);
                JavaClassModel exporterInterfaceClassModel = javaClassService.getByName(exporterClass);

                // Create the "source code" report for the Service Interface
                enableSourceReport(interfaceJavaClassModel);

                String tagName = getTagName(exporterClass);

                // Add the name to the Technological Tag Model, this will be used for Technologycal Usage Report
                TechnologyTagService technologyTagService = new TechnologyTagService(event.getGraphContext());
                technologyTagService.addTagToFileModel(interfaceJavaClassModel.getClassFile(), tagName, TechnologyTagLevel.INFORMATIONAL);

                SpringRemoteServiceModelService springRemoteRemoteServiceModelService = new SpringRemoteServiceModelService(event.getGraphContext());
                springRemoteRemoteServiceModelService.getOrCreate(typeReference.getFile().getApplication(), interfaceJavaClassModel, exporterInterfaceClassModel);

                // Create the "source code" report for the Implementation.
                enableSourceReport(implementationJavaClassModel);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.severe(e.getMessage());
        }
    }

    private JavaClassModel getImplementationClass(String implementationBean, GraphContext context) {
        return StreamSupport.stream(new SpringBeanService(context).findAllBySpringBeanName(implementationBean).spliterator(), false)
                .findFirst()
                .map(SpringBeanModel::getJavaClass)
                .orElse(null);
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
