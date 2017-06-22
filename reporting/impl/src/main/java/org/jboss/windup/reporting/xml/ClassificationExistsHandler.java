package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.ClassificationExists;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Creates a {@link ClassificationExists} that searches for the given classification text. Example usage:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *             &lt;not&gt;
 *                 &lt;classification-exists classification="JOnAS Web Descriptor" in="filename"/&gt;
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
@NamespaceElementHandler(elementName = ClassificationExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class ClassificationExistsHandler implements ElementHandler<ClassificationExists>
{
    static final String ELEMENT_NAME = "classification-exists";
    private static final String CLASSIFICATION = "classification";

    @Override
    public ClassificationExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String classificationPattern = $(element).attr(CLASSIFICATION);
        String in = $(element).attr("in");

        if (StringUtils.isBlank(classificationPattern))
        {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + CLASSIFICATION + "' attribute");
        }

        ClassificationExists classificationExists = ClassificationExists.withClassification(classificationPattern);
        return classificationExists.in(in);
    }
}
