package org.jboss.windup.reporting.xml;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.condition.HintExists;
import org.jboss.windup.reporting.config.condition.HintNotExists;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Creates a {@link HintExists} that searches for the given hint message.
 * Example usage to find all java class locations without hint with the given message
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *                 &lt;javaclass references="some.example.Jonas.descriptor"/&gt;
 *                 &lt;hint-not-exists message="JOnAS Descriptor usage" in="filename"/&gt;
 *         &lt;/when&gt;
 *         &lt;perform&gt;
 *             [...]
 *         &lt;/perform&gt;
 *     &lt;/rule&gt;
 * </pre>
 *
 * @author jsightler
 * @author mbriskar
 *
 */
@NamespaceElementHandler(elementName = HintNotExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class HintNotExistsHandler implements ElementHandler<HintExists>
{
    static final String ELEMENT_NAME = "hint-not-exists";
    private static final String MESSAGE = "message";

    @Override
    public HintExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String messagePattern = $(element).attr(MESSAGE);
        String in = $(element).attr("in");

        HintNotExists hintExists = HintNotExists.withMessage(messagePattern);
        return hintExists.in(in);
    }
}
