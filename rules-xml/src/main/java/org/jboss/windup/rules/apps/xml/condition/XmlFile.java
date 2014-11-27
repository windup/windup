package org.jboss.windup.rules.apps.xml.condition;

import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
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
import org.jboss.windup.reporting.model.FileReferenceModel;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.ExecutionStatistics;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.exception.WindupException;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.config.Condition;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlFile extends GraphCondition
{
    private static final Logger LOG = Logging.get(XmlFile.class);

    protected static final String UNPARSEABLE_XML_CLASSIFICATION = "Unparseable XML File";
    protected static final String UNPARSEABLE_XML_DESCRIPTION = "This file could not be parsed via XPath";
    private String variable = Iteration.DEFAULT_VARIABLE_LIST_STRING;
    private String xpath;
    private Map<String, String> namespaces = new HashMap<>();
    private String fileName;
    private String publicId;

    private String xpathResultMatch;

    public void setXpathResultMatch(String xpathResultMatch)
    {
        this.xpathResultMatch = xpathResultMatch;
    }

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

    public static XmlFile withDTDPublicId(String publicIdRegex)
    {
        XmlFile xmlFile = new XmlFile();
        xmlFile.publicId = publicIdRegex;
        return xmlFile;
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

    public XmlFile resultMatches(String regex)
    {
        this.xpathResultMatch = regex;
        return this;
    }

    /**
     * Specify the name of the variables to base this query on.
     * 
     * @param fromVariable
     * @return
     */
    public static XmlFileFrom from(String fromVariable)
    {
        return new XmlFileFrom(fromVariable);
    }

    @Override
    public boolean evaluate(final GraphRewrite event, final EvaluationContext context)
    {
        ExecutionStatistics.get().begin("XmlFile.evaluate");
        // list will cache all the created xpath matches for this given condition running
        List<WindupVertexFrame> resultLocations = new ArrayList<WindupVertexFrame>();
        GraphContext graphContext = event.getGraphContext();
        GraphService<XmlFileModel> xmlResourceService = new GraphService<XmlFileModel>(graphContext,
                    XmlFileModel.class);
        Iterable<? extends WindupVertexFrame> allXmls;
        if (getInputVariablesName() == null || getInputVariablesName().equals(""))
        {
            allXmls = xmlResourceService.findAll();
        }
        else
        {
            allXmls = Variables.instance(event).findVariable(getInputVariablesName());
        }

        for (WindupVertexFrame iterated : allXmls)
        {
            XmlFileModel xml = null;
            if (iterated instanceof FileReferenceModel)
            {
                xml = (XmlFileModel) ((FileReferenceModel) iterated).getFile();
            }
            else if (iterated instanceof XmlFileModel)
            {
                xml = (XmlFileModel) iterated;
            }
            else
            {
                throw new WindupException("XmlFile was called on the wrong graph type ( " + iterated.toPrettyString()
                            + ")");
            }

            if (fileName != null && !fileName.equals(""))
            {
                if (!xml.getFileName().matches(fileName))
                {
                    continue;
                }
            }
            if (publicId != null && !publicId.equals(""))
            {
                if (xml.getDoctype() == null || xml.getDoctype().getPublicId() == null
                            || !xml.getDoctype().getPublicId().matches(publicId))
                {
                    continue;
                }

            }
            if (xpath != null)
            {
                XmlFileService xmlFileService = new XmlFileService(graphContext);
                Document document = xmlFileService.loadDocumentQuiet(xml);
                if (document != null)
                {
                    NodeList result = XmlUtil.xpathNodeList(document, xpath, namespaces);
                    List<String> lines = null;
                    try
                    {
                        lines = Files.readAllLines(Paths.get(xml.getFilePath()), Charset.defaultCharset());
                    }
                    catch (Exception e)
                    {
                        LOG.log(Level.WARNING, "Could not read lines from: " + xml.getFilePath() + ", due to: " + e.getMessage(), e);
                    }
                    if (result != null && (result.getLength() != 0))
                    {
                        for (int i = 0; i < result.getLength(); i++)
                        {
                            Node node = result.item(i);
                            if (xpathResultMatch != null)
                            {
                                if (!node.toString().matches(xpathResultMatch))
                                {
                                    continue;
                                }
                            }
                            // Everything passed for this Node. Start creating XmlTypeReferenceModel for it.
                            int lineNumber = (int) node.getUserData(
                                        LocationAwareContentHandler.LINE_NUMBER_KEY_NAME);
                            int columnNumber = (int) node.getUserData(
                                        LocationAwareContentHandler.COLUMN_NUMBER_KEY_NAME);

                            int lineLength = lines == null ? 0 : lines.get(lineNumber - 1).length();
                            graphContext = event.getGraphContext();
                            GraphService<XmlTypeReferenceModel> fileLocationService = new GraphService<XmlTypeReferenceModel>(
                                        graphContext,
                                        XmlTypeReferenceModel.class);
                            XmlTypeReferenceModel fileLocation = fileLocationService.create();
                            fileLocation.setSourceSnippit(nodeToString(node));
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
                }
            }
        }
        Variables.instance(event).setVariable(variable, resultLocations);
        ExecutionStatistics.get().end("XmlFile.evaluate");
        return !resultLocations.isEmpty();
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
        this.publicId = publicId;
    }

    private static String nodeToString(Node node)
    {
        StringWriter sw = new StringWriter();
        try
        {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        }
        catch (TransformerException te)
        {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("XmlFile");
        if (getInputVariablesName() != null)
        {
            builder.append(".inputVariable(" + getInputVariablesName() + ")");
        }
        if (xpath != null)
        {
            builder.append(".matches(" + xpath + ")");
        }
        if (fileName != null)
        {
            builder.append(".inFile(" + fileName + ")");
        }
        if (publicId != null)
        {
            builder.append(".withDTDPublicId(" + publicId + ")");
        }
        builder.append(".as(" + variable + ")");
        return builder.toString();
    }
}
