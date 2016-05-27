package org.jboss.windup.rules.apps.xml.xml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.model.Severity;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.files.condition.ProcessingIsOnlineGraphCondition;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A rule provider validating all of the xml files and registering the classification in case the xml file is not valid.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ValidateXmlFilesRuleProvider extends AbstractRuleProvider
{
    public static final String NOT_VALID_XML_TAG = "Not valid XML";
    private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    private static final String XMLSCHEMA_ATTRIBUTE_NAME = "schemaLocation";
    private static final String NO_NAMESPACE_XMLSCHEMA_ATTRIBUTE_NAME = "noNamespaceSchemaLocation";

    public ValidateXmlFilesRuleProvider()
    {
        super(MetadataBuilder
                    .forProvider(ValidateXmlFilesRuleProvider.class)
                    .setPhase(MigrationRulesPhase.class)
                    .setHaltOnException(true));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(new ProcessingIsOnlineGraphCondition().and(Query.fromType(XmlFileModel.class)))
                    .perform(new ValidateAndRegisterClassification());
    }

    private class ValidateAndRegisterClassification extends AbstractIterationOperation<XmlFileModel>
    {

        @Override
        public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile)
        {
            boolean validationFailed = false;
            try
            {
                // ignore if we have already encountered a parse error
                if (StringUtils.isNotBlank(sourceFile.getParseError()))
                    return;

                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(true);
                factory.setXIncludeAware(false);

                final SAXParser parser = factory.newSAXParser();
                parser.setProperty(JAXP_SCHEMA_LANGUAGE, XMLConstants.W3C_XML_SCHEMA_NS_URI);

                final List<String> xsdURLs = new ArrayList<>();
                final List<SAXParseException> parseExceptions = new ArrayList<>();
                DefaultHandler2 handler = new DefaultHandler2()
                {
                    private final EnhancedEntityResolver2 entityResolver = new EnhancedEntityResolver2();
                    private boolean firstElementFound = false;
                    private Locator locator;

                    @Override
                    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException
                    {
                        return entityResolver.resolveEntity(publicId, systemId);
                    }

                    @Override
                    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException
                    {
                        return entityResolver.resolveEntity(name, publicId, baseURI, systemId);
                    }

                    @Override
                    public void setDocumentLocator(Locator locator)
                    {
                        this.locator = locator;
                    }

                    @Override
                    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
                    {
                        if (!firstElementFound)
                        {
                            firstElementFound = true;
                            try
                            {
                                xsdURLs.addAll(getXSDLocations(attributes));

                                // validate the xsd urls
                                for (String xsdUrl : xsdURLs)
                                {
                                    // this will throw if it invalid
                                    try  (InputStream is = new URL(xsdUrl).openStream())
                                    {
                                    }
                                    catch (IOException e)
                                    {
                                        throw new InvalidXSDURLException(e.getMessage(), xsdUrl);
                                    }
                                }
                            }
                            catch (InvalidXSDURLException e)
                            {
                                parseExceptions.add(new SAXParseException(e.getMessage(), locator, e));
                            }
                        }
                        super.startElement(uri, localName, qName, attributes);
                    }

                    @Override
                    public void warning(SAXParseException e) throws SAXException
                    {
                        super.warning(e);
                    }

                    @Override
                    public void error(SAXParseException e) throws SAXException
                    {
                        parseExceptions.add(e);
                    }

                    @Override
                    public void fatalError(SAXParseException e) throws SAXException
                    {
                        parseExceptions.add(e);
                    }
                };

                parser.parse(sourceFile.asFile(), handler);

                if (!xsdURLs.isEmpty())
                {
                    for (SAXParseException exception : parseExceptions)
                    {
                        validationFailed = true;
                        createSAXParseHint(event, context, sourceFile, exception);
                    }
                }
            }
            catch (SAXParseException e)
            {
                validationFailed = true;
                createSAXParseHint(event, context, sourceFile, e);
            }
            catch (ParserConfigurationException | IOException | SAXException e)
            {
                validationFailed = true;
            }
            finally
            {
                if (validationFailed)
                {
                    sourceFile.setGenerateSourceReport(true);

                    ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                    ClassificationModel model = classificationService.attachClassification(context, sourceFile, XmlFileModel.NOT_VALID_XML,
                                null);
                    TagSetService tagSetService = new TagSetService(event.getGraphContext());
                    model.setTagModel(tagSetService.getOrCreate(event, Collections.singleton(NOT_VALID_XML_TAG)));
                }
            }
        }

        private void createSAXParseHint(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile, SAXParseException e)
        {
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
            hintModel.setSeverity(Severity.POTENTIAL);

            if (e.getCause() instanceof InvalidXSDURLException)
            {
                String xsdUrl = ((InvalidXSDURLException) e.getCause()).getUrl();
                hintModel.setTitle(XmlFileModel.XSD_URL_NOT_VALID);
                hintModel.setHint(xsdUrl + " is not a valid url.");
            }
            else
            {
                hintModel.setTitle(XmlFileModel.NOT_VALID_XML);
                String message = "XSD Validation failed due to:\n\n";
                message += "\t" + e.getMessage();
                message += "\n\n";
                hintModel.setHint(message);
            }

            sourceFile.setGenerateSourceReport(true);
        }

        private List<String> getXSDLocations(Attributes attributes) throws InvalidXSDURLException
        {
            List<String> xsdUrls = new ArrayList<>();
            if (attributes == null)
            {
                return xsdUrls;
            }

            String xsdSchemaAttr = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                        XMLSCHEMA_ATTRIBUTE_NAME);
            if (StringUtils.isNotBlank(xsdSchemaAttr))
            {
                String[] splittedXslSchemaAttr = xsdSchemaAttr.split("\\s");
                for (int i = 1; i < splittedXslSchemaAttr.length; i += 2)
                {
                    String urlStr = splittedXslSchemaAttr[i];
                    xsdUrls.add(urlStr);
                }
            }

            String noNamespaceSchema = attributes.getValue(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
                        NO_NAMESPACE_XMLSCHEMA_ATTRIBUTE_NAME);
            if (StringUtils.isNotBlank(noNamespaceSchema))
            {
                xsdUrls.add(noNamespaceSchema);
            }
            return xsdUrls;
        }
    }
}
