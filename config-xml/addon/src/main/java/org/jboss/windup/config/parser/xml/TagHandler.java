package org.jboss.windup.config.parser.xml;

import org.apache.commons.lang3.StringUtils;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.util.exception.WindupException;
import org.w3c.dom.Element;

import static org.joox.JOOX.$;

/**
 * Takes any <tag>value<tag/> element and read its content
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
@NamespaceElementHandler(elementName = "tag", namespace = LabelProviderHandler.WINDUP_LABEL_NAMESPACE)
public class TagHandler implements ElementHandler<String>
{
    @Override
    public String processElement(ParserContext context, Element element)
    {
        String content = element.getTextContent();
        if (StringUtils.isBlank(content))
        {
            throw new WindupException("Error, 'tag' element must not be blank");
        }
        return content;
    }

}
