package org.jboss.windup.rules.apps.xml;

import static org.joox.JOOX.$;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.loader.RuleLoaderContext;
import org.jboss.windup.config.metadata.MetadataBuilder;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ClassifyFileTypesPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.service.DoctypeMetaService;
import org.jboss.windup.rules.apps.xml.service.NamespaceService;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.rules.files.FileMapping;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.LocationAwareContentHandler.Doctype;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;

/**
 * Extract some basic metadata from all {@link XmlFileModel}s found in the graph.
 */
public class DiscoverXmlFilesRuleProvider extends AbstractRuleProvider
{
    private static final Logger LOG = Logger.getLogger(DiscoverXmlFilesRuleProvider.class.getName());

    public DiscoverXmlFilesRuleProvider()
    {
        super(MetadataBuilder.forProvider(DiscoverXmlFilesRuleProvider.class)
                    .setPhase(ClassifyFileTypesPhase.class));
    }

    @Override
    public Configuration getConfiguration(RuleLoaderContext ruleLoaderContext)
    {
        return ConfigurationBuilder.begin()
            .addRule(FileMapping.from(".*\\.xml$").to(XmlFileModel.class))
            .addRule(FileMapping.from(".*\\.xmi$").to(XmlFileModel.class))
            .addRule(FileMapping.from(".*\\.jsf$").to(XmlFileModel.class))
            .addRule(FileMapping.from(".*\\.xhtml$").to(XmlFileModel.class))
            .addRule()
            .when(Query.fromType(XmlFileModel.class))
            .perform(new AbstractIterationOperation<XmlFileModel>()
            {
                @Override
                public void perform(GraphRewrite event, EvaluationContext context, XmlFileModel payload)
                {
                    addXmlMetaInformation(event, context, payload);
                }

                @Override
                public String toString()
                {
                    return "IndexXmlFilesMetadata";
                }
            });
    }

    private void addXmlMetaInformation(GraphRewrite event, EvaluationContext context, XmlFileModel file)
    {
        DoctypeMetaService docTypeService = new DoctypeMetaService(event.getGraphContext());
        NamespaceService namespaceService = new NamespaceService(event.getGraphContext());

        try
        {
            Document parsedDocument = file.asDocument();

            // pull out doctype data.
            Doctype docType = (Doctype) parsedDocument.getUserData(LocationAwareContentHandler.DOCTYPE_KEY_NAME);

            // if this is successful, then we know it is a proper XML file.
            // set it to the graph as an XML file.
            XmlFileModel xmlResourceModel = GraphService.addTypeToModel(event.getGraphContext(), file, XmlFileModel.class);

            // get and index by the root tag.
            String tagName = $(parsedDocument).tag();
            xmlResourceModel.setRootTagName(tagName);

            if (docType != null)
            {
                // create the doctype from
                Iterator<DoctypeMetaModel> metas = docTypeService.findByPublicIdAndSystemId(docType.getPublicId(),
                            docType.getSystemId());
                if (metas.hasNext())
                {
                    DoctypeMetaModel meta = metas.next();
                    meta.addXmlResource(xmlResourceModel);
                    xmlResourceModel.setDoctype(meta);
                }
                else
                {
                    DoctypeMetaModel meta = event.getGraphContext().getFramed().addVertex(null, DoctypeMetaModel.class);
                    meta.addXmlResource(xmlResourceModel);
                    meta.setBaseURI(docType.getBaseURI());
                    meta.setName(docType.getName());
                    meta.setPublicId(docType.getPublicId());
                    meta.setSystemId(docType.getSystemId());
                }
            }

            Map<String, String> namespaceSchemaLocations = XmlUtil.getSchemaLocations(parsedDocument);
            if (namespaceSchemaLocations != null && namespaceSchemaLocations.size() > 0)
            {
                for (String namespace : namespaceSchemaLocations.keySet())
                {
                    NamespaceMetaModel meta = namespaceService.createNamespaceSchemaLocation(namespace,
                                namespaceSchemaLocations.get(namespace));
                    meta.addXmlResource(xmlResourceModel);
                }
            }
        }
        catch (Exception e)
        {
            if (file.asFile().length() == 0)
            {
                final String message = "Failed to parse XML entity: " + file.getFilePath() + ": the file is empty.";
                LOG.log(Level.FINE, message);
                file.setParseError(message);
            }
            else
            {
                final String message = "Failed to parse XML entity: " + file.getFilePath() + ", due to: " + e.getMessage();
                LOG.log(Level.INFO, message);
                LOG.log(Level.FINE, message, e);
                file.setParseError(message);
            }
            new ClassificationService(event.getGraphContext())
                .attachClassification(event, context, file, XmlFileService.UNPARSEABLE_XML_CLASSIFICATION, XmlFileService.UNPARSEABLE_XML_DESCRIPTION);
        }
    }
}
