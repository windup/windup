package org.jboss.windup.rules.apps.xml;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A rule provider validating all of the xml files and registering the classification in case the xml file is not valid.
 */
public class ValidateXmlFilesRuleProvider extends AbstractRuleProvider
{

    public static final String XMLSCHEMA_URL = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XMLSCHEMA_ATTRIBUTE_NAME = "schemaLocation";

    public ValidateXmlFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(ValidateXmlFilesRuleProvider.class)
                    .setPhase(MigrationRulesPhase.class)
                    .setHaltOnException(true));
    }

    @Override
    public Configuration getConfiguration(GraphContext context)
    {
        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(Query.fromType(XmlFileModel.class))
                    .perform(new ValidateAndRegisterClassification());
    }

    private class ValidateAndRegisterClassification extends AbstractIterationOperation<XmlFileModel>
    {
        @Override
        public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel sourceFile)
        {
            XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
            Document document = xmlFileService.loadDocumentQuiet(context, sourceFile);
            String xsdUrl = getXSDSchemaLocation(document);
            try
            {
                if(xsdUrl != null && !xsdUrl.isEmpty() && !isXmlValid(sourceFile,xsdUrl)) {
                    ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                    classificationService.attachClassification(context, sourceFile, XmlFileModel.NOT_VALID_XML,
                                            XmlFileModel.NOT_VALID_XML);
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                e.printStackTrace();
            }

        }

        private String getXSDSchemaLocation(Document document) {
            String xsdSchemaAttr =document.getDocumentElement().getAttributeNS(XMLSCHEMA_URL, XMLSCHEMA_ATTRIBUTE_NAME);
            String[] splittedXslSchemaAttr = xsdSchemaAttr.split(" ");
            return splittedXslSchemaAttr[splittedXslSchemaAttr.length-1];
        }

        private boolean isXmlValid(XmlFileModel sourceFile, String xsdUrl) throws MalformedURLException, SAXException
        {
            URL schemaFile = new URL(xsdUrl);
            Source xmlFile = new StreamSource(sourceFile.asFile());
            SchemaFactory schemaFactory = SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            try {
                validator.validate(xmlFile);
            } catch (SAXException e) {
                return false;
            }
            catch (Exception e)
            {
                return false;
            }
            return true;
        }
    }
}
