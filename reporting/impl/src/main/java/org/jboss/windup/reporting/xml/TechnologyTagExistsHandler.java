package org.jboss.windup.reporting.xml;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.TechnologyTagExists;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Creates a {@link TechnologyTagExists} that searches for the given classification text. Example usage:
 *
 * <pre>
 *     &lt;rule&gt;
 *         &lt;when&gt;
 *             &lt;not&gt;
 *                 &lt;technology-tag-exists technology-tag="technologyname" in="filename"/&gt;
 *             &lt;/not&gt;
 *         &lt;/when&gt;
 *         &lt;perform&gt;
 *             [...]
 *         &lt;/perform&gt;
 *     &lt;/rule&gt;
 * </pre>
 *
 * @author mrizzi
 */
@NamespaceElementHandler(elementName = TechnologyTagExistsHandler.ELEMENT_NAME, namespace = "http://windup.jboss.org/schema/jboss-ruleset")
public class TechnologyTagExistsHandler implements ElementHandler<TechnologyTagExists> {
    static final String ELEMENT_NAME = "technology-tag-exists";
    private static final String NAME = "technology-tag";

    @Override
    public TechnologyTagExists processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String technologyTagPattern = $(element).attr(NAME);
        String in = $(element).attr("in");

        if (StringUtils.isBlank(technologyTagPattern)) {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + NAME + "' attribute");
        }

        return TechnologyTagExists.withName(technologyTagPattern).in(in);
    }
}
