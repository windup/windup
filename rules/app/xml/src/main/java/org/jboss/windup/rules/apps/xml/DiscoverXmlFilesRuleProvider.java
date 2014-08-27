package org.jboss.windup.rules.apps.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.ruleelement.AbstractIterationOperation;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.query.QueryPropertyComparisonType;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.resource.FileModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.DoctypeMetaModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlResourceModel;
import org.jboss.windup.rules.apps.xml.service.DoctypeMetaService;
import org.jboss.windup.rules.apps.xml.service.NamespaceService;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.LocationAwareContentHandler.Doctype;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import static org.joox.JOOX.$;

public class DiscoverXmlFilesRuleProvider extends WindupRuleProvider
{
    private static final Logger LOG = Logger.getLogger(DiscoverXmlFilesRuleProvider.class.getSimpleName());

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }
    public List<String> getExecuteAfterIDs()
    {
        List<String> ids = new ArrayList<String>();
        ids.add("org.jboss.windup.rules.apps:rules-java.UnzipArchivesToOutputRuleProvider");
        ids.add("org.jboss.windup.rules.apps:rules-java.ArchiveTypingRuleProvider");
        return ids;
    }

    @Override
    public Configuration getConfiguration(GraphContext arg0)
    {
        ConditionBuilder isXml = Query
                    .find(FileModel.class)
                    .withProperty(FileModel.IS_DIRECTORY, false)
                    .withProperty(FileModel.FILE_PATH, QueryPropertyComparisonType.REGEX, ".*\\.xml$");

        AbstractIterationOperation<FileModel> evaluatePomFiles = new AbstractIterationOperation<FileModel>()
        {
            @Override
            public void perform(GraphRewrite event, EvaluationContext context, FileModel payload)
            {
                addXmlMetaInformation(event.getGraphContext(), payload);
            }
        };

        return ConfigurationBuilder.begin()
                    .addRule()
                    .when(isXml)
                    .perform(evaluatePomFiles);
    }

    private void addXmlMetaInformation(GraphContext context, FileModel file)
    {
        DoctypeMetaService docTypeService = context.getService(DoctypeMetaModel.class);
        NamespaceService namespaceService = context.getService(NamespaceMetaModel.class);

        // try and read the XML...
        try (InputStream is = file.asInputStream())
        {

            // read it to a Document object.
            Document parsedDocument = LocationAwareXmlReader.readXML(is);

            // pull out doctype data.
            Doctype docType = (Doctype) parsedDocument.getUserData(LocationAwareContentHandler.DOCTYPE_KEY_NAME);

            // if this is successful, then we know it is a proper XML file.
            // set it to the graph as an XML file.
            XmlFileModel xmlResourceModel = GraphService.addTypeToModel(context, file, XmlFileModel.class);

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
                    DoctypeMetaModel meta = context.getFramed().addVertex(null, DoctypeMetaModel.class);
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
        catch (SAXException e)
        {
            LOG.log(Level.WARNING, "Failed to parse xml entity: " + file.getFilePath() + ", due to: " + e.getMessage(),
                        e);
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING,
                        "Failed to parse xml entity: " + file.getFilePath() + ", due to: " + e.getMessage(), e);
        }
        catch (Exception e)
        {
            throw new WindupException("Failed to load and parse XML for entity: " + file.getFilePath(), e);
        }
    }
}
