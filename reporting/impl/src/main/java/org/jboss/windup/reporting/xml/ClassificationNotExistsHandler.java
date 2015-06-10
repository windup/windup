package org.jboss.windup.reporting.xml;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.condition.ClassificationExists;
import org.jboss.windup.reporting.config.condition.ClassificationNotExists;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;


/**
 * Creates a {@link ClassificationNotExists} that searches for the given classification text.
 * Example usage to find all jonas web descriptors without classification:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *                 &lt;xmlfile matches="//jonas" in="jonas.web.xml"/&gt;
 *                 &lt;classification-not-exists title="JOnAS Web Descriptor"/&gt;
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
@NamespaceElementHandler(elementName = ClassificationNotExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class ClassificationNotExistsHandler implements ElementHandler<ClassificationExists>
{
    static final String ELEMENT_NAME = "classification-not-exists";
    private static final String CLASSIFICATION = "title";

    @Override
    public ClassificationExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String classificationPattern = $(element).attr(CLASSIFICATION);
        String in = $(element).attr("in");

        ClassificationNotExists classificationExists = ClassificationNotExists.withClassification(classificationPattern);
        return classificationExists.in(in);
    }
}
