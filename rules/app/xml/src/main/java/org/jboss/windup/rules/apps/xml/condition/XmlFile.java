package org.jboss.windup.rules.apps.xml.condition;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.forge.furnace.util.Assert;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.Variables;
import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.graph.service.Service;
import org.jboss.windup.reporting.model.ClassificationModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlResourceModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.util.exception.MarshallingException;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.config.Rule;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class XmlFile extends GraphCondition
{
    protected static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    protected static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed via XPath";
    private String variable = Iteration.DEFAULT_VARIABLE_LIST_STRING;;

    private String xpath;
    private Map<String, String> namespaces = new HashMap<>();
    private String fileName;
    private String fromVariables;
    private String publicId;

    private XmlFile(String xpath)
    {
        this.xpath = xpath;
    }

    XmlFile()
    {
    }

    /**
     * Create a new {@link XmlFile} {@link Condition}.
     */
    public static XmlFile matchesXpath(String xpath)
    {
        return new XmlFile(xpath);
    }

    public XmlFile havingDTDPublicId(String regex)
    {
        this.publicId = regex;
        return this;
    }

    public ConditionBuilder as(String variable)
    {
        Assert.notNull(variable, "Variable name must not be null.");
        this.variable = variable;
        return this;
    }

    public XmlFile inFile(String fileName)
    {
        this.fileName = fileName;
        return this;
    }

    /**
     * Specify the name of the variables to base this query on.
     * 
     * @param fromVariables
     * @return
     */
    public static XmlFileBeing being(String fromVariables)
    {
        return new XmlFileBeing(fromVariables);
    }

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        // list will cache all the created xpath matches for this given condition running
        List<WindupVertexFrame> resultLocations = new ArrayList<WindupVertexFrame>();
        GraphContext graphContext = event.getGraphContext();
        GraphService<XmlResourceModel> xmlResourceService = new GraphService<XmlResourceModel>(graphContext,
                    XmlResourceModel.class);
        Iterable<? extends WindupVertexFrame> allXmls;
        if (fromVariables == null || fromVariables.equals(""))
        {
            allXmls = xmlResourceService.findAll();
        }
        else
        {
            allXmls = Variables.instance(event).findVariable(fromVariables);
        }

        for (WindupVertexFrame iterated : allXmls)
        {
            XmlResourceModel xml = (XmlResourceModel) iterated;
            if (fileName != null && !fileName.equals(""))
            {
                if (!xml.getFileName().equals(fileName))
                {
                    continue;
                }
            }
            if (publicId != null && !publicId.equals(""))
            {
                if (xml.getDoctype() == null || !xml.getDoctype().getPublicId().matches(publicId))
                {
                    continue;
                }

            }
            try
            {
                Document document = xml.asDocument();
                NodeList result = XmlUtil.xpathNodeList(document, xpath, namespaces);
                if (result != null && (result.getLength() != 0))
                {
                    int lineNumber = (int) result.item(0).getUserData(LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
                    int columnNumber = (int) result.item(0).getUserData(
                                LocationAwareContentHandler.COLUMN_NUMBER_KEY_NAME);
                    String documentString = getStringFromDocument(document);
                    String[] strings = documentString.split("\n");
                    int lineLength = strings[lineNumber - 1].length();
                    graphContext = event.getGraphContext();
                    GraphService<XmlTypeReferenceModel> fileLocationService = new GraphService<XmlTypeReferenceModel>(
                                graphContext,
                                XmlTypeReferenceModel.class);
                    XmlTypeReferenceModel fileLocation = fileLocationService.create();
                    fileLocation.setLineNumber(lineNumber);
                    fileLocation.setColumnNumber(columnNumber);
                    fileLocation.setLength(lineLength);
                    fileLocation.setFile(xml);
                    fileLocation.setXpath(xpath);
                    GraphService<NamespaceMetaModel> metaModelService = new GraphService<NamespaceMetaModel>(
                                graphContext,
                                NamespaceMetaModel.class);
                    for (Map.Entry<String, String> namespace : namespaces.entrySet())
                    {
                        NamespaceMetaModel metaModel = metaModelService.create();
                        metaModel.setSchemaLocation(namespace.getKey());
                        metaModel.setSchemaLocation(namespace.getValue());
                        metaModel.addXmlResource(xml);
                        fileLocation.addNamespace(metaModel);
                    }
                    resultLocations.add(fileLocation);
                }
            }
            catch (TransformerException|MarshallingException e)
            {
                // TODO: In case of bad xpath, this exception is raised also
                Service<ClassificationModel> classificationService = event.getGraphContext().getService(
                            ClassificationModel.class);

                ClassificationModel classification = classificationService.getUniqueByProperty(
                            ClassificationModel.PROPERTY_CLASSIFICATION, XmlFile.UNPARSEABLE_XML_CLASSIFICATION);

                if (classification == null)
                {
                    classification = classificationService.create();
                    classification.setDescription(XmlFile.UNPARSEABLE_XML_DESCRIPTION);
                    classification.setClassifiation(XmlFile.UNPARSEABLE_XML_CLASSIFICATION);

                    // TODO replace this with a link to a RuleModel, if that gets implemented.
                    classification.setRuleID(((Rule) context.get(Rule.class)).getId());
                }

                classification.addFileModel(xml);
            }

        }
        Variables.instance(event).setVariable(variable, resultLocations);
        return !resultLocations.isEmpty();
    }

    public String getStringFromDocument(Document doc) throws TransformerException
    {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public XmlFile namespace(String prefix, String url)
    {
        namespaces.put(prefix, url);
        return this;
    }

    public void setXpath(String xpath)
    {
        this.xpath = xpath;
    }

    public void setPublicId(String publicId)
    {
        this.publicId=publicId;
    }

}
