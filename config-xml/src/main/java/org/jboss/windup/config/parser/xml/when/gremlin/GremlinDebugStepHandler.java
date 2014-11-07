package org.jboss.windup.config.parser.xml.when.gremlin;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.gremlinquery.DebugStep;
import org.jboss.windup.config.gremlinquery.GremlinQuery;
import org.jboss.windup.config.gremlinquery.GremlinStep;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

/**
 * Parses "debug" elements to add {@link GremlinStep}s to the current {@link GremlinQuery}.
 * 
 * @author jsightler
 */
@NamespaceElementHandler(elementName = "debug", namespace = "http://windup.jboss.org/v1/xml")
public class GremlinDebugStepHandler implements ElementHandler<DebugStep>
{

    @Override
    public DebugStep processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String levelsStr = $(element).attr("levels");
        int levels = 0;
        if (!StringUtils.isBlank(levelsStr))
        {
            levels = Integer.parseInt(levelsStr);
        }
        return DebugStep.output(levels);
    }
}
