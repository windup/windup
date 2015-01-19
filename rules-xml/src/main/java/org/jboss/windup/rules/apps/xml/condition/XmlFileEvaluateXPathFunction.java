package org.jboss.windup.rules.apps.xml.condition;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.jboss.windup.config.condition.EvaluationStrategy;
import org.jboss.windup.util.Logging;

class XmlFileEvaluateXPathFunction implements XPathFunction
{
    private static final Logger LOG = Logging.get(XmlFileEvaluateXPathFunction.class);

    private final EvaluationStrategy evaluationStrategy;

    XmlFileEvaluateXPathFunction(EvaluationStrategy evaluationStrategy)
    {
        this.evaluationStrategy = evaluationStrategy;
    }

    @Override
    public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException
    {
        int frameIdx = ((Double) args.get(0)).intValue();
        boolean expressionResult = (Boolean) args.get(1);
        LOG.fine("evaluate(" + frameIdx + ", " + expressionResult + ")");
        if (!expressionResult)
        {
            evaluationStrategy.modelSubmissionRejected();
        }
        return expressionResult;
    }
}