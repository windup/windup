package org.jboss.windup.rules.apps.xml.xml;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.MigrationRulesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.reporting.service.InlineHintService;
import org.jboss.windup.reporting.service.TagSetService;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * A rule provider validating all of the xml files and registering the classification in case the xml file is not valid.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class ValidateXmlFilesRuleProvider extends AbstractRuleProvider
{

    public static final String XMLSCHEMA_URL = "http://www.w3.org/2001/XMLSchema-instance";
    public static final String XMLSCHEMA_ATTRIBUTE_NAME = "schemaLocation";
    public static final String NOT_VALID_XML_TAG = "Not valid XML";

    SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    public ValidateXmlFilesRuleProvider()
    {
        this(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI));
    }

    public ValidateXmlFilesRuleProvider(SchemaFactory schemaFactory)
    {
        super(MetadataBuilder.forProvider(ValidateXmlFilesRuleProvider.class)
                    .setPhase(MigrationRulesPhase.class)
                    .setHaltOnException(true));
        this.schemaFactory=schemaFactory;
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
            XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
            Document document = xmlFileService.loadDocumentQuiet(context, sourceFile);
            String xsdUrl = getXSDSchemaLocationUrl(document);
            try
            {
                if(xsdUrl != null && !xsdUrl.isEmpty() && !isXmlValid(sourceFile,xsdUrl)) {
                    ClassificationService classificationService = new ClassificationService(event.getGraphContext());
                    ClassificationModel model =classificationService.attachClassification(context, sourceFile, XmlFileModel.NOT_VALID_XML,
                                null);
                    TagSetService tagSetService = new TagSetService(event.getGraphContext());
                    model.setTagModel(tagSetService.getOrCreate(event, Collections.singleton(NOT_VALID_XML_TAG)));
                }
            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            }
            catch (SAXException e)
            {
                final Throwable cause = e.getCause();
                if(cause instanceof UnknownHostException) {
                    //probably no internet connection. Do not do anything
                } else if(cause instanceof FileNotFoundException) {
                    //probably wrong XSD URL
                    FileLocationModel rootElementLocation = createLocationModelFromNodeElement(document.getDocumentElement(),event.getGraphContext(),sourceFile);

                    final InlineHintModel inlineHintModel = InlineHintService.addTypeToModel(event.getGraphContext(), rootElementLocation, InlineHintModel.class);
                    inlineHintModel.setTitle(XmlFileModel.XSD_URL_NOT_VALID);
                    inlineHintModel.setHint(xsdUrl + " is not a valid url.");
                    inlineHintModel.setEffort(1);
                }
            }

        }

        private FileLocationModel createLocationModelFromNodeElement(Node node, GraphContext context, FileModel sourceFile) {
            int lineNumber = (int) node.getUserData(
                        LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
            int columnNumber = (int) node.getUserData(
                        LocationAwareContentHandler.COLUMN_NUMBER_KEY_NAME);

            GraphService<FileLocationModel> fileLocationService = new GraphService<>(
                        context,
                        FileLocationModel.class);
            FileLocationModel fileLocation = fileLocationService.create();
            String sourceSnippit = XmlUtil.nodeToString(node);
            fileLocation.setSourceSnippit(sourceSnippit);
            fileLocation.setLineNumber(lineNumber);
            fileLocation.setColumnNumber(columnNumber);
            fileLocation.setLength(node.toString().length());
            fileLocation.setFile(sourceFile);
            return fileLocation;
        }

        private String getXSDSchemaLocationUrl(Document document) {
            if(document == null) {
                return null;
            }

            String xsdSchemaAttr =document.getDocumentElement().getAttributeNS(XMLSCHEMA_URL, XMLSCHEMA_ATTRIBUTE_NAME);
            if(xsdSchemaAttr == null) {
                return null;
            }
            String[] splittedXslSchemaAttr = xsdSchemaAttr.split(" ");
            return splittedXslSchemaAttr[splittedXslSchemaAttr.length-1];
        }

        private boolean isXmlValid(XmlFileModel sourceFile, String xsdUrl) throws MalformedURLException, SAXException
        {
            URL schemaFile = new URL(xsdUrl);
            Source xmlFile = new StreamSource(sourceFile.asFile());
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
