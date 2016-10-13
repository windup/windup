package org.jboss.windup.rules.apps.xml.condition.validators;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.xml.condition.XmlFile;
import org.jboss.windup.rules.apps.xml.condition.XmlFileEvaluateXPathFunction;
import org.jboss.windup.rules.apps.xml.condition.XmlFileFunctionResolver;
import org.jboss.windup.rules.apps.xml.condition.XmlFileMatchesXPathFunction;
import org.jboss.windup.rules.apps.xml.condition.XmlFileParameterMatchCache;
import org.jboss.windup.rules.apps.xml.condition.XmlFileStartFrameXPathFunction;
import org.jboss.windup.rules.apps.xml.condition.XmlFileXPathTransformer;
import org.jboss.windup.rules.apps.xml.model.NamespaceMetaModel;
import org.jboss.windup.rules.apps.xml.model.XmlFileModel;
import org.jboss.windup.rules.apps.xml.model.XmlTypeReferenceModel;
import org.jboss.windup.rules.apps.xml.service.XmlFileService;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.xml.LocationAwareContentHandler;
import org.jboss.windup.util.xml.NamespaceMapContext;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.DefaultParameterStore;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import javax.xml.xpath.XPathFunctionResolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This is a part of XmlFile execution. Validator is checking that xpath/xpathResult attributes matches the queried one.
 */
public class XmlFileXpathValidator implements XmlFileValidator
{
    protected static final String WINDUP_NS_PREFIX = "windup";
    protected static final String WINDUP_NS_URI = "http://windup.jboss.org/windupv2functions";
    private static XPathFactory factory = XPathFactory.newInstance();
    private XPathExpression compiledXPath;
    private final XPath xpathEngine;
    private final XmlFileFunctionResolver xmlFileFunctionResolver;
    private XmlFile.XmlFileEvaluationStrategy evaluationStrategy;
    private String xpathResultMatch;
    private List<WindupVertexFrame> results = new ArrayList<>();
    private Map<String, String> namespaces = new HashMap<>();
    private String xpathString;
    private static final Logger LOG = Logging.get(XmlFileXpathValidator.class);
    private RegexParameterizedPatternParser xpathPattern;

    public XmlFileXpathValidator()
    {

        this.namespaces.put(WINDUP_NS_PREFIX, WINDUP_NS_URI);

        this.xpathEngine = factory.newXPath();
        final XPathFunctionResolver originalResolver = this.xpathEngine.getXPathFunctionResolver();
        xmlFileFunctionResolver = new XmlFileFunctionResolver(originalResolver);
        this.xpathEngine.setXPathFunctionResolver(xmlFileFunctionResolver);
    }

    public void setXpathString(String xpath)
    {
        this.xpathString = xpath;
        this.compiledXPath = null;

        if (xpath != null)
        {
            this.xpathPattern = new RegexParameterizedPatternParser(this.xpathString);
        }
    }

    @Override
    public boolean isValid(GraphRewrite event, EvaluationContext context, XmlFileModel model)
    {
        if(xpathString == null) {
            return true;
        }
        String xpathStringWithParameterFunctions = XmlFileXPathTransformer.transformXPath(this.xpathString);
        LOG.fine("XmlFile compiled: " + this.xpathString + " to " + xpathStringWithParameterFunctions);

        XmlFileService xmlFileService = new XmlFileService(event.getGraphContext());
        Document document = xmlFileService.loadDocumentQuiet(context, model);
        if (document != null)
        {
            final ParameterStore store = DefaultParameterStore.getInstance(context);

            final XmlFileParameterMatchCache paramMatchCache = new XmlFileParameterMatchCache();
            this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "startFrame",
                        new XmlFileStartFrameXPathFunction(paramMatchCache));
            this.xmlFileFunctionResolver
                        .registerFunction(WINDUP_NS_URI, "evaluate", new XmlFileEvaluateXPathFunction(evaluationStrategy));
            this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "matches", new XmlFileMatchesXPathFunction(context, store,
                        paramMatchCache, event));
            this.xmlFileFunctionResolver.registerFunction(WINDUP_NS_URI, "persist", new XmlFilePersistXPathFunction(event, context, model,
                        evaluationStrategy, store, paramMatchCache, results));

            if (compiledXPath == null)
            {
                NamespaceMapContext nsContext = new NamespaceMapContext(namespaces);
                this.xpathEngine.setNamespaceContext(nsContext);
                try
                {
                    this.compiledXPath = xpathEngine.compile(xpathStringWithParameterFunctions);
                }
                catch (Exception e)
                {
                    String message = e.getMessage();

                    // brutal hack to try to get a reasonable error message (ugly, but it seems to work)
                    if (message == null && e.getCause() != null && e.getCause().getMessage() != null)
                    {
                        message = e.getCause().getMessage();
                    }
                    LOG.severe("Condition: " + this + " failed to run, as the following xpath was uncompilable: " + xpathString
                                + " (compiled contents: " + xpathStringWithParameterFunctions + ") due to: "
                                + message);
                    return false;
                }
            }

            /**
             * This actually does the work.
             */
            XmlUtil.xpathNodeList(document, compiledXPath);
            evaluationStrategy.modelSubmissionRejected();
        }
        return !results.isEmpty();
    }

    public List<WindupVertexFrame> getAndClearResultLocations()
    {
        List<WindupVertexFrame> output = results;
        results = new ArrayList<WindupVertexFrame>();
        return output;
    }

    public void setXpathResult(String xpathResult)
    {
        this.xpathResultMatch = xpathResult;
    }

    public String getXpathString()
    {
        return xpathString;
    }

    public XPathExpression getXpathExpression()
    {
        return compiledXPath;
    }

    public void setParameterStore(ParameterStore store)
    {
        if (this.xpathPattern != null)
        {
            this.xpathPattern.setParameterStore(store);
        }
    }

    public Collection<? extends String> getRequiredParamaterNames()
    {
        if (xpathPattern != null)
        {
            return xpathPattern.getRequiredParameterNames();
        }
        else
        {
            return Collections.emptyList();
        }

    }

    public void addNamespace(String prefix, String url)
    {
        this.namespaces.put(prefix, url);
    }

    public void setEvaluationStrategy(XmlFile.XmlFileEvaluationStrategy evaluationStrategy)
    {
        this.evaluationStrategy = evaluationStrategy;
    }

    final class XmlFilePersistXPathFunction implements XPathFunction
    {
        private final GraphRewrite event;
        private final EvaluationContext context;
        private final XmlFileModel xml;
        private final XmlFile.XmlFileEvaluationStrategy evaluationStrategy;
        private final ParameterStore store;
        private final XmlFileParameterMatchCache paramMatchCache;
        private final List<WindupVertexFrame> resultLocations;

        XmlFilePersistXPathFunction(GraphRewrite event, EvaluationContext context, XmlFileModel xml,
                    XmlFile.XmlFileEvaluationStrategy evaluationStrategy,
                    ParameterStore store,
                    XmlFileParameterMatchCache paramMatchCache, List<WindupVertexFrame> resultLocations)
        {
            this.event = event;
            this.context = context;
            this.xml = xml;
            this.evaluationStrategy = evaluationStrategy;
            this.store = store;
            this.paramMatchCache = paramMatchCache;
            this.resultLocations = resultLocations;
        }

        @Override
        public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException
        {
            int frameIdx = ((Double) args.get(0)).intValue();
            NodeList arg1 = (NodeList) args.get(1);
            String nodeText = XmlUtil.nodeListToString(arg1);
            LOG.fine("persist(" + frameIdx + ", " + nodeText + ")");

            for (int i = 0; i < arg1.getLength(); i++)
            {
                Node node = arg1.item(i);
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

                GraphService<XmlTypeReferenceModel> fileLocationService = new GraphService<>(
                            event.getGraphContext(),
                            XmlTypeReferenceModel.class);
                XmlTypeReferenceModel fileLocation = fileLocationService.create();
                String sourceSnippit = XmlUtil.nodeToString(node);
                fileLocation.setSourceSnippit(sourceSnippit);
                fileLocation.setLineNumber(lineNumber);
                fileLocation.setColumnNumber(columnNumber);
                fileLocation.setLength(node.toString().length());
                fileLocation.setFile(xml);
                fileLocation.setXpath(xpathString);
                GraphService<NamespaceMetaModel> metaModelService = new GraphService<>(
                            event.getGraphContext(),
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

                evaluationStrategy.modelSubmissionRejected();
                evaluationStrategy.modelMatched();

                for (Map.Entry<String, String> entry : paramMatchCache.getVariables().entrySet())
                {
                    Parameter<?> param = store.get(entry.getKey());
                    String value = entry.getValue();
                    if (!evaluationStrategy.submitValue(param, value))
                    {
                        evaluationStrategy.modelSubmissionRejected();
                        return false;
                    }
                }
                evaluationStrategy.modelSubmitted(fileLocation);
                evaluationStrategy.modelMatched();
            }

            return true;
        }
    }
}
