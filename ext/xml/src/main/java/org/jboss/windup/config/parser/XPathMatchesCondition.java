package org.jboss.windup.config.parser;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.jboss.windup.config.ConfigurationException;
import org.jboss.windup.config.GraphRewrite;

import org.jboss.windup.config.condition.GraphCondition;
import org.jboss.windup.graph.model.resource.XmlResourceModel;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.w3c.dom.Document;


public class XPathMatchesCondition extends GraphCondition
{
    protected final XPathExpression xpathExpression;

    public XPathMatchesCondition(String pattern, NamespaceContext context)
    {
        final XPathFactory xPathFactory = XPathFactory.newInstance();
        final XPath xpath = xPathFactory.newXPath();
        xpath.setNamespaceContext(context);
        try {
            xpathExpression = xpath.compile(pattern);
        }
        catch (final XPathExpressionException e) {
            throw new ConfigurationException("Exception parsing the XPath pattern: " + pattern, e);
        }
    }

    @Override
    public boolean evaluate(GraphRewrite event, EvaluationContext context)
    {
        if( ! (event.getResource() instanceof XmlResourceModel) )
            return false;

        final XmlResourceModel resource = (XmlResourceModel) event.getResource();
        try
        {
            final Document document = resource.asDocument();
            final Boolean result = (Boolean) xpathExpression.evaluate(document, XPathConstants.BOOLEAN);
            return result;
        }
        catch (final Exception e)
        {
            throw new RuntimeException("Exception evaluating rule.", e);
        }
    }
}
