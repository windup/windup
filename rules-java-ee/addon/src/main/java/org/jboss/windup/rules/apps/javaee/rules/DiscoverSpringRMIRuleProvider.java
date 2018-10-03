package org.jboss.windup.rules.apps.javaee.rules;

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
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
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

@RuleMetadata(phase = MigrationRulesPhase.class)
public class DiscoverSpringRMIRuleProvider extends AbstractRuleProvider {
    private static final Logger LOG = Logging.get(DiscoverRmiRuleProvider.class);

    @Override
    public Configuration getConfiguration(RuleLoaderContext context) {
        return ConfigurationBuilder
                .begin()
                .addRule(getXMLBeanRule());
    }

    private Rule getXMLBeanRule() {
        return RuleBuilder.define()
                .when(XmlFile.matchesXpath("//bean[@class=\"org.springframework.remoting.rmi.RmiServiceExporter\"]"))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .withId(getClass().getSimpleName() + "_SpringRMIRule");
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

        RMIServiceModelService rmiService = new RMIServiceModelService(event.getGraphContext());

        try {
            // we obtain the XML fragment with the RMI Exporter Bean
            Document xmlDocSnippet = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(typeReference.getSourceSnippit())));

            String interfaceName = $(xmlDocSnippet).xpath("//property[@name=\"serviceInterface\"]").first().attr("value");
            String implementationBean = $(xmlDocSnippet).xpath("//property[@name=\"service\"]").first().attr("ref");

            // we obtain the Whole XML Document to find the implementation Bean
            Document wholeDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(typeReference.getFile().asInputStream());
            String implementationClass = $(wholeDocument).xpath("//bean[@id=\"" + implementationBean + "\"]").first().attr("class") ;

            JavaClassService javaClassService = new JavaClassService(event.getGraphContext());
            JavaClassModel interfaceJavaClassModel = javaClassService.getByName(interfaceName);
            // Create the "source code" report for the Service Interface
            interfaceJavaClassModel.getDecompiledSource().setGenerateSourceReport(true);

            RMIServiceModel rmiServiceModel = rmiService.getOrCreate(typeReference.getFile().getApplication(), interfaceJavaClassModel);

            // Create the "source code" report for the RMI Implementation.
            if (rmiServiceModel != null && rmiServiceModel.getImplementationClass() != null) {
                for (AbstractJavaSourceModel source : javaClassService.getJavaSource(implementationClass)) {
                    source.setGenerateSourceReport(true);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            LOG.severe(e.getMessage());
        }
    }
}
