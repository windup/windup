package org.jboss.windup.rules.apps.xml.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.XMLDocumentCache;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Manages creating, querying, and deleting XmlFileModels.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author Ondrej Zizka
 */
public class XmlFileService extends GraphService<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(XmlFileService.class.getName());

    public final static String UNPARSEABLE_XML_CLASSIFICATION = "Unparsable XML File";
    public final static String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed";


    public XmlFileService(GraphContext ctx)
    {
        super(ctx, XmlFileModel.class);
    }

    /**
     * Loads and parses the provided XML file. This will quietly fail (not throwing an {@link Exception}) and return
     * null if it is unable to parse the provided {@link XmlFileModel}. A {@link ClassificationModel} will be created to
     * indicate that this file failed to parse.
     *
     * @return Returns either the parsed {@link Document} or null if the {@link Document} could not be parsed
     */
    public Document loadDocumentQuiet(GraphRewrite event, EvaluationContext context, XmlFileModel model)
    {
        try
        {
            return loadDocument(event, context, model);
        }
        catch(Exception ex)
        {
            return null;
        }
    }

    /**
     * Loads and parses the provided XML file. This will quietly fail (not throwing an {@link Exception}) and return
     * null if it is unable to parse the provided {@link XmlFileModel}. A {@link ClassificationModel} will be created to
     * indicate that this file failed to parse.
     *
     * @return Returns either the parsed {@link Document} or null if the {@link Document} could not be parsed
     */
    public Document loadDocument(GraphRewrite event, EvaluationContext context, XmlFileModel model) throws WindupException
    {
        if (model.asFile().length() == 0)
        {
            final String msg = "Failed to parse, XML file is empty: " + model.getFilePath();
            LOG.log(Level.WARNING, msg);
            model.setParseError(msg);
            throw new WindupException(msg);
        }

        ClassificationService classificationService = new ClassificationService(getGraphContext());

        XMLDocumentCache.Result cacheResult = XMLDocumentCache.get(model);
        if (cacheResult.isParseFailure())
        {
            final String msg = "Not loading XML file '" + model.getFilePath() + "' due to previous parse failure: " + model.getParseError();
            LOG.log(Level.FINE, msg);
            //model.setParseError(msg);
            throw new WindupException(msg);
        }

        Document document = cacheResult.getDocument();
        if (document != null)
            return document;

        // Not yet cached - load, store in cache and return.
        try (InputStream is = model.asInputStream())
        {
            document = LocationAwareXmlReader.readXML(is);
            XMLDocumentCache.cache(model, document);
        }
        catch (SAXException | IOException e)
        {
            XMLDocumentCache.cacheParseFailure(model);
            document = null;
            final String message = "Failed to parse XML file: " + model.getFilePath() + ", due to: " + e.getMessage();
            LOG.log(Level.WARNING, message);
            classificationService.attachClassification(event, context, model, UNPARSEABLE_XML_CLASSIFICATION, UNPARSEABLE_XML_DESCRIPTION);
            model.setParseError(message);
            throw new WindupException(message, e);
        }

        return document;
    }
}
