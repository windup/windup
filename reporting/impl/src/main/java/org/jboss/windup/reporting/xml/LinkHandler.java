package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Link} operation to the currently selected items.
 * <p>
 * Expected format:
 *
 * <pre>
 *  &lt;link href="http://www.foo.com/" description="Helpful text" /&gt;
 * </pre>
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 */
@NamespaceElementHandler(elementName = LinkHandler.ELEMENT_NAME, namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class LinkHandler implements ElementHandler<Link> {
    static final String ELEMENT_NAME = "link";
    private static final String HREF_ATTR = "href";
    private static final String TITLE_ATTR = "title";

    @Override
    public Link processElement(ParserContext handlerManager, Element element) throws ConfigurationException {
        String link = $(element).attr(HREF_ATTR);
        if (StringUtils.isBlank(link)) {
            throw new WindupException("Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + HREF_ATTR
                    + "' attribute (eg, 'http://www.example.com/somepage.html')");
        }
        String title = $(element).attr(TITLE_ATTR);
        if (StringUtils.isBlank(title)) {
            throw new WindupException(
                    "Error, '" + ELEMENT_NAME + "' element must have a non-empty '" + TITLE_ATTR
                            + "' attribute (eg, 'Documentation for XYZ')");
        }
        return Link.to(title, link);
    }
}
