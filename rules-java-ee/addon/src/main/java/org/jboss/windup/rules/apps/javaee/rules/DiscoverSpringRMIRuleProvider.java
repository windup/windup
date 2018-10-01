package org.jboss.windup.rules.apps.javaee.rules;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.rules.apps.java.condition.JavaClass;
import org.jboss.windup.rules.apps.java.model.AbstractJavaSourceModel;
import org.jboss.windup.rules.apps.java.model.JavaClassModel;
import org.jboss.windup.rules.apps.java.scan.ast.JavaTypeReferenceModel;
import org.jboss.windup.rules.apps.java.service.JavaClassService;
import org.jboss.windup.rules.apps.javaee.model.RMIServiceModel;
import org.jboss.windup.rules.apps.javaee.service.RMIServiceModelService;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.config.RuleBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.logging.Logger;

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
                .when(XmlFile.matchesXpath("//bean [@id=//bean[@class=\"org.springframework.remoting.rmi.RmiServiceExporter\"]/property[@name=\"service\"]/@ref]"))
                .perform(Iteration.over()
                        .perform(addSpringRMIBeanToGraph())
                        .endIteration())
                .withId(getClass().getSimpleName() + "_SpringRMIRule");
    }

    private AbstractIterationOperation<XmlFileModel> addSpringRMIBeanToGraph() {
        return new AbstractIterationOperation<XmlFileModel>() {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel typeReference)
            {
                extractMetadata(event, typeReference);
            }
        };
    }

    private void extractMetadata(GraphRewrite event, XmlFileModel typeReference) {

        LOG.info("Processing: " + typeReference);
        // Make sure we create a source report for the interface source
        typeReference.setGenerateSourceReport(true);

        RMIServiceModelService rmiService = new RMIServiceModelService(event.getGraphContext());

        String className = typeReference.asDocument().getElementsByTagName("bean").item(0).getAttributes().getNamedItem("class").getNodeValue();

        JavaClassService javaClassService = new JavaClassService(event.getGraphContext());

        RMIServiceModel rmiServiceModel = rmiService.getOrCreate(typeReference.getApplication(), javaClassService.getByName(className));

    }
}
