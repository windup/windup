package org.jboss.windup.config.parser.xml;

import org.jboss.windup.config.metadata.Label;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

import static org.joox.JOOX.$;

/**
 * Handles parsing the "labels" element
 *
 * @author <a href="mailto:carlosthe19916@gmail.com">Carlos Feria</a>
 */
@NamespaceElementHandler(elementName = "labels", namespace = LabelProviderHandler.WINDUP_LABEL_NAMESPACE)
public class LabelsHandler implements ElementHandler<List<Label>>
{
    @Override
    public List<Label> processElement(ParserContext context, Element element)
    {
        List<Label> labels = new ArrayList<>();

        List<Element> children = $(element).children().get();
        for (Element child : children)
        {
            labels.add(context.processElement(child));
        }

        return labels;
    }

}
