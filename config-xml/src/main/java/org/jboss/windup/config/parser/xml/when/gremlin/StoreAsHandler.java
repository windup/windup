package org.jboss.windup.config.parser.xml.when.gremlin;

import static org.joox.JOOX.$;

import org.jboss.windup.config.Variables;
import org.jboss.windup.config.exception.ConfigurationException;
import org.jboss.windup.config.gremlinquery.GremlinQuery;
import org.jboss.windup.config.gremlinquery.StoreAs;
import org.jboss.windup.config.parser.ElementHandler;
import org.jboss.windup.config.parser.NamespaceElementHandler;
import org.jboss.windup.config.parser.ParserContext;
import org.w3c.dom.Element;

/**
 * Provides an easy way to store the current state at a particular point in a {@link GremlinQuery} to a variables inside of {@link Variables}.
 * 
 * @author jsightler
 */
@NamespaceElementHandler(elementName = "store", namespace = "http://windup.jboss.org/v1/xml")
public class StoreAsHandler implements ElementHandler<StoreAs>
{

    @Override
    public StoreAs processElement(ParserContext handlerManager, Element element) throws ConfigurationException
    {
        String outputVar = $(element).attr("output");
        return new StoreAs(outputVar);
    }
}
