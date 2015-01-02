package org.jboss.windup.reporting.xml;

import static org.joox.JOOX.$;

import org.apache.commons.lang.StringUtils;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.reporting.config.Link;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

/**
 * Adds the provided {@link Link} operation to the currently selected items.
 * 
 * Expected format:
 * 
 * <pre>
 *  &lt;link href="http://www.foo.com/" description="Helpful text" /&gt;
 * </pre>
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 */
@NamespaceElementHandler(elementName = "link", namespace = "http://windup.jboss.org/v1/xml")
public class LinkHandler implements ElementHandler<Link>
{

    @Override
    public Link processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String link = $(element).attr("href");
        if (StringUtils.isBlank(link))
        {
            throw new WindupException(
                        "Error, 'link' element must have a non-empty 'href' attribute (eg, 'http://www.example.com/somepage.html')");
        }
        String description = $(element).attr("description");
        if (StringUtils.isBlank(description))
        {
            throw new WindupException(
                        "Error, 'link' element must have a non-empty 'description' attribute (eg, 'Helpful tips on migrating Spring Beans')");
        }
        return (Link)Link.to(description, link);
    }
}
