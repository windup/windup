package org.jboss.windup.rules.apps.xml.xml;

import java.io.IOException;
import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.service.WindupConfigurationService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.reporting.category.IssueCategoryRegistry;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.files.condition.ProcessingIsOnlineGraphCondition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * A rule provider validating all of the xml files and registering the classification in case the xml file is not valid.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 * @author <a href="mailto:hotmana76@gmail.com">Marek Novotny</a>
 */
public class ValidateXmlFilesRuleProvider extends AbstractRuleProvider {
    public static final String NOT_VALID_XML_TAG = "Not valid XML";
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";


    public ValidateXmlFilesRuleProvider() {
        super(MetadataBuilder
                .forProvider(ValidateXmlFilesRuleProvider.class)
                .setPhase(MigrationRulesPhase.class)
                .setHaltOnException(true));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext) {
        return ConfigurationBuilder.begin()
                .addRule()
                .when(new ProcessingIsOnlineGraphCondition().and(Query.fromType(XmlFileModel.class)))
                .perform(new ValidateAndRegisterClassification());
    }

    private class ValidateAndRegisterClassification extends AbstractIterationOperation<XmlFileModel> {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile) {
            boolean onlineMode = WindupConfigurationService.getConfigurationModel(event.getGraphContext()).isOnlineMode();
            boolean validationFailed = false;
            try {
                // ignore if we have already encountered a parse error
                if (StringUtils.isNotBlank(sourceFile.getParseError()))
                    return;

                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                factory.setXIncludeAware(false);

                final SAXParser parser = factory.newSAXParser();
                parser.setProperty(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);

                ValidateXmlHandler handler = new ValidateXmlHandler(onlineMode);

                parser.parse(sourceFile.asFile(), handler);

                if (!handler.getXsdURLs().isEmpty()) {
                    for (SAXParseException exception : handler.getParseExceptions()) {
                        if (isExceptionRelatedToSource(sourceFile, exception)) {
                            validationFailed = true;
                            createSAXParseHint(event, context, sourceFile, exception);
                        }
                    }
                }
            } catch (SAXParseException e) {
                if (isExceptionRelatedToSource(sourceFile, e)) {
                    validationFailed = true;
                    createSAXParseHint(event, context, sourceFile, e);
                }
            } catch (ParserConfigurationException | IOException | SAXException e) {
                validationFailed = true;
            } finally {
                if (validationFailed)
                    createParseFailureClassification(event, context, sourceFile);
            }
        }

        private boolean isExceptionRelatedToSource(XmlFileModel sourceFile, SAXParseException e) {
            if (e.getSystemId() == null)
                return true;  // Just assume that it is related, in the absence of other information

            if (e.getSystemId().startsWith("http://") || e.getSystemId().startsWith("https://") || e.getSystemId().startsWith("ftp://"))
                return false;

            return e.getSystemId().endsWith(sourceFile.getFileName());
        }

        private void createSAXParseHint(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile, SAXParseException e) {
            int lineNumber = e.getLineNumber();
            int column = e.getColumnNumber();

            InlineHintService service = new InlineHintService(event.getGraphContext());
            InlineHintModel hintModel = service.create();
            hintModel.setRuleID(((Rule) context.get(Rule.class)).getId());
            hintModel.setLineNumber(lineNumber);
            hintModel.setColumnNumber(column);

            // FIXME - Fake value as we don't get an actual length of the error from the parser
            hintModel.setLength(1);
            hintModel.setFile(sourceFile);
            hintModel.setEffort(1);

            IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
            hintModel.setIssueCategory(issueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.POTENTIAL));

            if (e.getCause() instanceof InvalidXSDURLException) {
                String xsdUrl = ((InvalidXSDURLException) e.getCause()).getUrl();
                hintModel.setTitle(XmlFileModel.XSD_URL_NOT_VALID);
                hintModel.setHint(xsdUrl + " is not a valid url.");
            } else {
                hintModel.setTitle(XmlFileModel.NOT_VALID_XML);
                String message = "XSD Validation failed due to:\n\n";
                message += "\t" + e.getMessage();
                message += System.lineSeparator() + System.lineSeparator();
                hintModel.setHint(message);
            }

            sourceFile.setGenerateSourceReport(true);
        }
    }

    private void createParseFailureClassification(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile) {
        sourceFile.setGenerateSourceReport(true);

        ClassificationService classificationService = new ClassificationService(event.getGraphContext());
        ClassificationModel model = classificationService.attachClassification(event, context, sourceFile, XmlFileModel.NOT_VALID_XML,
                null);
        model.setEffort(0); // do not rely on default 0 value and set it that transparently

        IssueCategoryRegistry issueCategoryRegistry = IssueCategoryRegistry.instance(event.getRewriteContext());
        model.setIssueCategory(issueCategoryRegistry.loadFromGraph(event.getGraphContext(), IssueCategoryRegistry.POTENTIAL));

        TagSetService tagSetService = new TagSetService(event.getGraphContext());
        model.setTagModel(tagSetService.getOrCreate(event, Collections.singleton(NOT_VALID_XML_TAG)));
    }
}
