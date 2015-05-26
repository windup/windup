package org.jboss.windup.config.parser.metadata;

import static org.joox.JOOX.$;

import java.util.ArrayList;
import java.util.List;

import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.jboss.windup.config.parser.xml.RuleProviderHandler;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "tags", namespace = RuleProviderHandler.WINDUP_RULE_NAMESPACE)
public class MetadataTagHandler implements ElementHandler<List<String>>
{

    @Override
    public List<String> processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        List<Element> children = $(element).children().get();
        
        List<String> tags = new ArrayList<>();
        for (Element child : children)
        {
            switch ($(child).tag())
            {
            case "tag":
                tags.add(child.getTextContent());
                break;
            }
           
        }
        return tags;
    }
    
}
