package org.jboss.windup.rules.apps.xml.condition;

import java.util.List;
import java.util.logging.Logger;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;

import org.jboss.windup.util.Logging;

class XmlFileStartFrameXPathFunction implements XPathFunction
{
    private static Logger LOG = Logging.get(XmlFileStartFrameXPathFunction.class);

    private final XmlFileParameterMatchCache paramMatchCache;

    XmlFileStartFrameXPathFunction(XmlFileParameterMatchCache paramMatchCache)
    {
        this.paramMatchCache = paramMatchCache;
    }

    @Override
    public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException
    {
        int frameIdx = ((Double) args.get(0)).intValue();
        LOG.fine("startFrame(" + frameIdx + ")!");
        paramMatchCache.addFrame(frameIdx);
        return true;
    }
}