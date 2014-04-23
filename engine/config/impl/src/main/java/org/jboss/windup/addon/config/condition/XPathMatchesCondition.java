package org.jboss.windup.addon.config.condition;

import javax.inject.Inject;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.jboss.windup.addon.config.ConfigurationException;
import org.jboss.windup.addon.config.GraphRewrite;
import org.jboss.windup.graph.dao.XmlResourceDao;
import org.jboss.windup.graph.model.resource.XmlResource;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class XPathMatchesCondition extends GraphCondition
{

    private static final Logger LOG = LoggerFactory.getLogger(XPathMatchesCondition.class);

    @Inject
    XmlResourceDao xmlDao;

    protected final XPathExpression xpathExpression;

    public XPathMatchesCondition(String pattern, NamespaceContext context)
    {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(context);
        try
        {
            xpathExpression = xpath.compile(pattern);
        }
        catch (XPathExpressionException e)
        {
            throw new ConfigurationException("Exception parsing the XPath pattern: " + pattern, e);
        }
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        LOG.debug("Event [" + event.toString() + "]");
        if (event.getResource() instanceof XmlResource)
        {
            LOG.debug("Event [" + event.toString() + "] XPath [" + xpathExpression.toString() + "]");
            XmlResource resource = (XmlResource) event.getResource();
            try
            {
                Document document = resource.asDocument();
                Boolean result = (Boolean) xpathExpression.evaluate(document, XPathConstants.BOOLEAN);
                return result;
            }
            catch (Exception e)
            {
                throw new RuntimeException("Exception evaluating rule.", e);
            }
        }
        return false;
    }

}
