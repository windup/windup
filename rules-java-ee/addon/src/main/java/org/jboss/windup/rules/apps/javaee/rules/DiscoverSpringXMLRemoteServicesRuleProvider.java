package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.JaxWSWebServiceModel;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.service.JaxWSWebServiceModelService;
import org.jboss.windup.rules.apps.javaee.service.RMIServiceModelService;
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
using the ".remoting" package pattern in the class for the exporter, in XML files
 */
@RuleMetadata(phase = MigrationRulesPhase.class)
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
            public void perform(GraphRewrite event, EvaluationContext context, XmlTypeReferenceModel typeReference)
            {
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
                Document wholeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(typeReference.getFile().asInputStream());
                String implementationClass = getImplementationClass(implementationBean, wholeDocument);

                JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
                JavaClassModel interfaceJavaClassModel = javaClassService.getByName(interfaceName);
                JavaClassModel implementationJavaClassModel = javaClassService.getByName(implementationClass);

                // Create the "source code" report for the Service Interface
                if (interfaceJavaClassModel.getOriginalSource() != null) {
                    interfaceJavaClassModel.getOriginalSource().setGenerateSourceReport(true);
                } else {
                    interfaceJavaClassModel.getDecompiledSource().setGenerateSourceReport(true);
                }

                addClassToSection(exporterClass, event, typeReference, implementationJavaClassModel, interfaceJavaClassModel);
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.severe(e.getMessage());
        }
    }

    private void addClassToSection(String exporterClass, GraphRewrite event, XmlTypeReferenceModel typeReference, JavaClassModel implementationJavaClassModel, JavaClassModel interfaceJavaClassModel) {
        if (exporterClass.contains("RmiServiceExporter")) {
            addClassToRMISection(event, typeReference, implementationJavaClassModel, interfaceJavaClassModel);
        } else { //if (exporterClass.contains("JaxWsPortProxyFactoryBean")) {
            addClassToJaxWSSection(event, typeReference, implementationJavaClassModel, interfaceJavaClassModel);
        }
    }


    private void addClassToRMISection(GraphRewrite event, XmlTypeReferenceModel typeReference, JavaClassModel implementationClass, JavaClassModel interfaceJavaClassModel) {
        RMIServiceModelService rmiService = new RMIServiceModelService(event.getGraphContext());
        RMIServiceModel serviceModel = rmiService.getOrCreate(typeReference.getFile().getApplication(), interfaceJavaClassModel);

        // Create the "source code" report for the Implementation.
        if (serviceModel != null ) {
            if (implementationClass.getOriginalSource() != null) {
                implementationClass.getOriginalSource().setGenerateSourceReport(true);
            } else {
                implementationClass.getDecompiledSource().setGenerateSourceReport(true);
            }
        }
    }

    private void addClassToJaxWSSection(GraphRewrite event, XmlTypeReferenceModel typeReference, JavaClassModel implementationClass, JavaClassModel interfaceJavaClassModel) {
        JaxWSWebServiceModelService jaxwsService = new JaxWSWebServiceModelService(event.getGraphContext());
        JaxWSWebServiceModel serviceModel = jaxwsService.getOrCreate(typeReference.getFile().getApplication(), interfaceJavaClassModel, implementationClass);


        // Create the "source code" report for the Implementation.
        if (serviceModel != null) {
            if (implementationClass.getOriginalSource() != null) {
                implementationClass.getOriginalSource().setGenerateSourceReport(true);
            } else {
                implementationClass.getDecompiledSource().setGenerateSourceReport(true);
            }
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
