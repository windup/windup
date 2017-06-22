package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.HintExists;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Creates a {@link HintExists} that searches for the given hint message. Example usage:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *             &lt;not&gt;
 *                 &lt;hint-exists message="JOnAS Web Descriptor" in="filename"/&gt;
 *             &lt;/not&gt;
 *         &lt;/when&gt;
 *         &lt;perform&gt;
 *             [...]
 *         &lt;/perform&gt;
 *     &lt;/rule&gt;
 * </pre>
 * 
 * @author jsightler
 *
 */
@NamespaceElementHandler(elementName = HintExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class HintExistsHandler implements ElementHandler<HintExists>
{
    static final String ELEMENT_NAME = "hint-exists";
    private static final String MESSAGE = "message";

    @Override
    public HintExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String messagePattern = $(element).attr(MESSAGE);
        String in = $(element).attr("in");

        if (StringUtils.isBlank(messagePattern))
        {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + MESSAGE + "' attribute");
        }

        HintExists hintExists = HintExists.withMessage(messagePattern);
        return hintExists.in(in);
    }
}
