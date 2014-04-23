package org.jboss.windup.addon.config.parser.xml.when;

import org.jboss.windup.addon.config.ConfigurationException;
import org.jboss.windup.addon.config.condition.FilenameMatchesCondition;
import org.jboss.windup.addon.config.parser.ElementHandler;
import org.jboss.windup.addon.config.parser.NamespaceElementHandler;
import org.jboss.windup.addon.config.parser.ParserContext;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "filename", namespace = "http://windup.jboss.org/v1/xml")
public class FilenameMatchesHandler implements ElementHandler<FilenameMatchesCondition>
{

    @Override
    public FilenameMatchesCondition processElement(ParserContext handlerManager, Element element)
                throws ConfigurationException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
