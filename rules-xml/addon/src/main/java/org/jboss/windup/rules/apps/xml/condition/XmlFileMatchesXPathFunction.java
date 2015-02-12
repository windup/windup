package org.jboss.windup.rules.apps.xml.condition;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.util.Logging;
import org.jboss.windup.util.xml.XmlUtil;
import org.ocpsoft.rewrite.context.EvaluationContext;
import org.ocpsoft.rewrite.param.Parameter;
import org.ocpsoft.rewrite.param.ParameterStore;
import org.ocpsoft.rewrite.param.ParameterizedPatternResult;
import org.ocpsoft.rewrite.param.RegexParameterizedPatternParser;
import org.w3c.dom.NodeList;

class XmlFileMatchesXPathFunction implements XPathFunction
{
    private static Logger LOG = Logging.get(XmlFileMatchesXPathFunction.class);

    private final EvaluationContext context;
    private final ParameterStore store;
    private final XmlFileParameterMatchCache paramMatchCache;
    private final GraphRewrite event;

    XmlFileMatchesXPathFunction(EvaluationContext context, ParameterStore store, XmlFileParameterMatchCache paramMatchCache, GraphRewrite event)
    {
        this.context = context;
        this.store = store;
        this.paramMatchCache = paramMatchCache;
        this.event = event;
    }

    @Override
    public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException
    {
        int frameIdx = ((Double) args.get(0)).intValue();
        NodeList arg1 = (NodeList) args.get(1);
        String nodeText = XmlUtil.nodeListToString(arg1);
        String patternString = (String) args.get(2);
        LOG.fine("matches(" + frameIdx + ", " + nodeText + ", " + patternString + ")");
        RegexParameterizedPatternParser paramPattern = new RegexParameterizedPatternParser(patternString);
        paramPattern.setParameterStore(store);
        ParameterizedPatternResult referenceResult = paramPattern.parse(nodeText);

        boolean refMatches = referenceResult.isValid(event, context);
        if (!refMatches)
        {
            return false;
        }
        boolean refSubmitOk = true;
        for (Map.Entry<Parameter<?>, String> paramEntry : referenceResult.getParameters(context).entrySet())
        {
            String name = paramEntry.getKey().getName();
            if (!paramMatchCache.checkVariable(frameIdx, name, paramEntry.getValue()))
            {
                refSubmitOk = false;
                break;
            }
        }

        if (!refSubmitOk)
        {
            return false;
        }

        for (Map.Entry<Parameter<?>, String> paramEntry : referenceResult.getParameters(context).entrySet())
        {
            String name = paramEntry.getKey().getName();
            String value = paramEntry.getValue();
            paramMatchCache.addVariable(frameIdx, name, value);
        }
        return refSubmitOk;
    }
}