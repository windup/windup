package org.jboss.windup.config.parser.xml.perform;

import static org.joox.JOOX.$;

import org.jboss.windup.config.ConfigurationException;
import org.jboss.windup.config.operation.Log;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.ocpsoft.logging.Logger.Level;
import org.ocpsoft.rewrite.config.Operation;
import org.w3c.dom.Element;

@NamespaceElementHandler(elementName = "log", namespace = "http://windup.jboss.org/v1/xml")
public class LogHandler implements ElementHandler<Operation>
{
    @Override
    public Operation processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String message = $(element).attr("message");
        return Log.message(Level.INFO, message);
    }
}
