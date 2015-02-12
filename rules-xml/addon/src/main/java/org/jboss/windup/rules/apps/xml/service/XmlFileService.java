package org.jboss.windup.rules.apps.xml.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.reporting.service.ClassificationService;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.util.xml.LocationAwareXmlReader;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Manages creating, querying, and deleting XmlFileModels.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
public class XmlFileService extends GraphService<XmlFileModel>
{
    private static final Logger LOG = Logger.getLogger(XmlFileService.class.getSimpleName());

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
    public Document loadDocumentQuiet(XmlFileModel model)
    {
        ClassificationService classificationService = new ClassificationService(getGraphContext());
        try (InputStream is = model.asInputStream())
        {
            Document doc = LocationAwareXmlReader.readXML(is);
            return doc;
        }
        catch (SAXException e)
        {
            LOG.log(Level.WARNING,
                        "Failed to parse xml entity: " + model.getFilePath() + ", due to: " + e.getMessage(),
                        e);
            classificationService.attachClassification(model, XmlFileModel.UNPARSEABLE_XML_CLASSIFICATION,
                        XmlFileModel.UNPARSEABLE_XML_DESCRIPTION);
        }
        catch (IOException e)
        {
            LOG.log(Level.WARNING,
                        "Failed to parse xml entity: " + model.getFilePath() + ", due to: " + e.getMessage(), e);
            classificationService.attachClassification(model, XmlFileModel.UNPARSEABLE_XML_CLASSIFICATION,
                        XmlFileModel.UNPARSEABLE_XML_DESCRIPTION);
        }
        return null;
    }
}
