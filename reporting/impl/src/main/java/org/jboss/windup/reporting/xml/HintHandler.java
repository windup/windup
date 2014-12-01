package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Classification;
import org.jboss.windup.reporting.config.Hint;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Classification} operation to the currently selected items.
 * 
 * Expected format:
 * 
 * <pre>
 * &lt;hint message="hint" effort="8"&gt;
 * &lt;/hint&gt;
 * </pre>
 * 
 * Alternatively, the hint can be in its own element. This is primary useful for longer hint content:
 * 
 * <pre>
 * &lt;hint effort="8"&gt;
 *  &lt;message&gt;
 *          Longer help contents go here
 *  &lt;/message&gt;
 * &lt;/hint&gt;
 * </pre>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "hint", namespace = "http://windup.jboss.org/v1/xml")
public class HintHandler implements ElementHandler<Hint>
{

    @Override
    public Hint processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String message = $(element).attr("message");
        String in = $(element).attr("in");
        if(in == null) {
            in = Iteration.DEFAULT_SINGLE_VARIABLE_STRING;
        }

        if (StringUtils.isBlank(message))
        {
            List<Element> children = $(element).children().get();
            for (Element child : children)
            {
                if (child.getNodeName().equals("message"))
                {
                    message = handlerManager.processElement(child);
                }
            }
        }

        if (StringUtils.isBlank(message))
        {
            throw new WindupException("Error, 'hint' element must have a non-empty 'message' attribute or element");
        }

        String effortStr = $(element).attr("effort");
        
        Hint hint = Hint.in(in).withText(message);
        if (!StringUtils.isBlank(effortStr))
        {
            try
            {
                int effort = Integer.parseInt(effortStr);
                hint.withEffort(effort);
            }
            catch (NumberFormatException e)
            {
                throw new WindupException("Could not parse effort level: " + effortStr + " as an integer!");
            }
        }

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            if (child.getNodeName().equals("link"))
            {
                Link link = handlerManager.processElement(child);
                hint.with(link);
            }
        }
        return hint;
    }
}
