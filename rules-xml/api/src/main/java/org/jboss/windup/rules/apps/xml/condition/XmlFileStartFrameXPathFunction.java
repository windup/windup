package org.jboss.windup.rules.apps.xml.condition;

import org.jboss.windup.util.Logging;

import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionException;
import java.util.List;
import java.util.logging.Logger;

public class XmlFileStartFrameXPathFunction implements XPathFunction {
    private static final Logger LOG = Logging.get(XmlFileStartFrameXPathFunction.class);

    private final XmlFileParameterMatchCache paramMatchCache;

    public XmlFileStartFrameXPathFunction(XmlFileParameterMatchCache paramMatchCache) {
        this.paramMatchCache = paramMatchCache;
    }

    @Override
    public Object evaluate(@SuppressWarnings("rawtypes") List args) throws XPathFunctionException {
        int frameIdx = ((Double) args.get(0)).intValue();
        LOG.fine("startFrame(" + frameIdx + ")!");
        paramMatchCache.addFrame(frameIdx);
        return true;
    }
}